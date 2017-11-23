/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.kv4j.generic.backend.dynamodb;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.google.common.collect.Lists;
import com.schibsted.security.strongbox.sdk.internal.access.IAMPolicyManager;
import com.schibsted.security.strongbox.sdk.exceptions.PotentiallyMaliciousDataException;
import com.schibsted.security.strongbox.sdk.exceptions.FieldAccessException;
import com.schibsted.security.strongbox.sdk.exceptions.ParseException;
import com.schibsted.security.strongbox.sdk.exceptions.UnexpectedStateException;
import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;
import com.schibsted.security.strongbox.sdk.internal.RegionLocalResourceName;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.GenericStore;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend.KVStream;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend.RSEF;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend.SecretEventStream;
import com.schibsted.security.strongbox.sdk.internal.converter.Converters;
import com.schibsted.security.strongbox.sdk.internal.interfaces.ManagedResource;
import com.schibsted.security.strongbox.sdk.exceptions.AlreadyExistsException;
import com.schibsted.security.strongbox.sdk.exceptions.DoesNotExistException;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.annotation.Attribute;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.annotation.PartitionKey;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.annotation.SortKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manage DynamoDB tables and store/retrieve data entries
 *
 * @author stiankri
 * @author kvlees
 */
public class GenericDynamoDB<Entry, Primary> implements GenericStore<Entry, Primary>, ManagedResource, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(GenericDynamoDB.class);
    private static final int SLEEP_TIME = 500;
    private static final int MAX_RETRIES = 30;

    private AmazonDynamoDB client;
    private AWSCredentialsProvider awsCredentials;
    private ClientConfiguration clientConfiguration;
    private final String tableName;
    private Converters converters;
    private final Region region;

    private final ReadWriteLock readWriteLock;

    private final String SCHEMA_VERSION_FIELD_NAME = "0";
    private final String SCHEMA_VERSION = "1";
    private final String OPTIMISTIC_LOCK_FIELD_NAME = "optimisticLock";

    Class<Entry> clazz;
    Map<Integer, String> attributeMappings = new HashMap<>();
    Map<Integer, String> partitionKeyMapping = new HashMap<>();
    Integer partitionKeyName;
    Map<Integer, String> sortKeyMapping = new HashMap<>();
    Integer sortKeyName;

    private RSEF.PartitionKey<Entry, Primary> partitionKeyRef;

    public GenericDynamoDB(AmazonDynamoDB client, AWSCredentialsProvider awsCredentials,
                           ClientConfiguration clientConfiguration,
                           SecretsGroupIdentifier groupIdentifier, Class<Entry> clazz, Converters converters,
                           ReadWriteLock readWriteLock) {
        this.clazz = clazz;
        buildMappings();
        this.converters = converters;
        this.awsCredentials = awsCredentials;
        this.clientConfiguration = clientConfiguration;
        this.client = client;
        this.region = RegionUtils.getRegion(groupIdentifier.region.getName());
        this.readWriteLock = readWriteLock;

        RegionLocalResourceName resourceName = new RegionLocalResourceName(groupIdentifier);
        this.tableName = resourceName.toString();
    }

    private void waitForTableToBecomeActive() {
        int retries = 0;
        String tableStatus = "Unknown";
        try {
            while (retries < MAX_RETRIES) {
                log.info("Waiting for table to become active...");
                Thread.sleep(SLEEP_TIME);
                DescribeTableResult result = client.describeTable(tableName);
                tableStatus = result.getTable().getTableStatus();

                if (tableStatus.equals(TableStatus.ACTIVE.toString()) ||
                        tableStatus.equals(TableStatus.UPDATING.toString())) {
                    return;
                }

                if (tableStatus.equals(TableStatus.DELETING.toString())) {
                    throw new UnexpectedStateException(
                            tableName, tableStatus, TableStatus.ACTIVE.toString(),
                            "Table state changed to 'DELETING' before creation was confirmed");
                }
                retries++;
            }
        } catch (InterruptedException e) {
            throw new UnexpectedStateException(tableName, tableStatus, TableStatus.ACTIVE.toString(),
                                               "Error occurred while waiting for DynamoDB table", e);
        }
        throw new UnexpectedStateException(tableName, tableStatus, TableStatus.ACTIVE.toString(),
                                           "DynamoDB table did not become active before timeout");
    }

     private void waitForTableToFinishDeleting() {
         String tableStatus = "Unknown";
         try {
            int retries = 0;
            while (retries < MAX_RETRIES) {
                log.info("Waiting for table to be deleted...");
                Thread.sleep(SLEEP_TIME);
                DescribeTableResult result = client.describeTable(tableName);
                tableStatus = result.getTable().getTableStatus();
                retries++;
            }
         } catch(ResourceNotFoundException e) {
            // The table has been successfully deleted, so return
            return;
        } catch (InterruptedException e) {
            throw new UnexpectedStateException(tableName, tableStatus, "DELETED",
                                               "Error occurred while waiting for DynamoDB table", e);
        }
        throw new UnexpectedStateException(tableName, tableStatus, "DELETED",
                                           "DynamoDB table was not deleted before timeout");
    }

    public CreateTableRequest constructCreateTableRequest() {
        ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName(partitionKeyName.toString()).withAttributeType("S"));
        attributeDefinitions.add(new AttributeDefinition().withAttributeName(sortKeyName.toString()).withAttributeType("N"));

        ArrayList<KeySchemaElement> keySchema = new ArrayList<>();
        keySchema.add(new KeySchemaElement().withAttributeName(partitionKeyName.toString()).withKeyType(KeyType.HASH));
        keySchema.add(new KeySchemaElement().withAttributeName(sortKeyName.toString()).withKeyType(KeyType.RANGE));

        ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
                .withReadCapacityUnits(1L)
                .withWriteCapacityUnits(1L);
        CreateTableRequest request = new CreateTableRequest()
                .withTableName(tableName)
                .withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(provisionedThroughput);
        return request;
    }

    @Override
    public String create() {
        readWriteLock.writeLock().lock();

        try {
            CreateTableResult result = client.createTable(constructCreateTableRequest());
            waitForTableToBecomeActive();
            return result.getTableDescription().getTableArn();
        } catch (ResourceInUseException e) {
            throw new AlreadyExistsException(String.format("There is already a DynamoDB table called '%s'", tableName), e);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void delete() {
        readWriteLock.writeLock().lock();

        try {
            client.deleteTable(tableName);
            waitForTableToFinishDeleting();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public Optional<String> awsAdminPolicy() {
        return Optional.of(
                "    {\n" +
                "        \"Sid\": \"DynamoDB\",\n" +
                "        \"Effect\": \"Allow\",\n" +
                "        \"Action\": [\n" +
                "            \"dynamodb:*\"\n" +
                "        ],\n" +
                "        \"Resource\": \"" + getArn() + "\"\n" +
                "    }");
    }

    @Override
    public Optional<String> awsReadOnlyPolicy() {
        return Optional.of(
                "    {\n" +
                "        \"Sid\": \"DynamoDB\",\n" +
                "        \"Effect\": \"Allow\",\n" +
                "        \"Action\": [\n" +
                "            \"dynamodb:Query\",\n" +
                "            \"dynamodb:Scan\"\n" +
                "        ],\n" +
                "        \"Resource\": \"" + getArn() + "\"\n" +
                "    }");
    }

    @Override
    public String getArn() {
        return String.format("arn:aws:dynamodb:%s:%s:table/%s", region.getName(), IAMPolicyManager.getAccount(awsCredentials, clientConfiguration), tableName);
    }

    @Override
    public boolean exists() {
        readWriteLock.readLock().lock();

        try {
            return describeTable().isPresent();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private Optional<TableDescription> describeTable() {
        try {
            DescribeTableResult result = client.describeTable(tableName);
            return Optional.of(result.getTable());
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        }

    }

    private void buildMappings() {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Attribute[] attributes = field.getAnnotationsByType(Attribute.class);
            PartitionKey[] partitionKey = field.getAnnotationsByType(PartitionKey.class);
            SortKey[] sortKey = field.getAnnotationsByType(SortKey.class);

            if (attributes.length > 0) {
                int position = attributes[0].position();
                attributeMappings.put(position, field.getName());
            }

            if (partitionKey.length > 0) {
                int position = partitionKey[0].position();
                partitionKeyMapping.put(position, field.getName());
                this.partitionKeyName = position;
                this.partitionKeyRef = new RSEF.PartitionKey<>(position);
            }

            if (sortKey.length > 0) {
                int name = sortKey[0].position();
                sortKeyMapping.put(name, field.getName());
                this.sortKeyName = name;
            }
        }
    }

    @Override
    public void create(Entry entry) {
        readWriteLock.writeLock().lock();

        try {
            Map<String, AttributeValue> keys = createKey(entry);
            Map<String, AttributeValueUpdate> attributes = createAttributes(entry);
            Map<String, ExpectedAttributeValue> expected = expectNotExists();

            try {
                executeUpdate(keys, attributes, expected);
            } catch (ConditionalCheckFailedException e) {
                throw new AlreadyExistsException("DynamoDB store entry already exists:" + keys.toString());
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void update(Entry entry, Entry existingEntry) {
        readWriteLock.writeLock().lock();

        try {
            Map<String, AttributeValue> keys = createKey(entry);
            Map<String, AttributeValueUpdate> attributes = createAttributes(entry);
            Map<String, ExpectedAttributeValue> expected = expectExists(existingEntry);

            try {
                executeUpdate(keys, attributes, expected);
            } catch (ConditionalCheckFailedException e) {
                throw new DoesNotExistException("Precondition to update entry in DynamoDB failed:" + keys.toString());
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    private void executeUpdate(Map<String, AttributeValue> keys, Map<String, AttributeValueUpdate> attributes, Map<String, ExpectedAttributeValue> expected) {
        UpdateItemRequest updateEntry = new UpdateItemRequest()
                .withTableName(tableName)
                .withKey(keys)
                .withAttributeUpdates(attributes)
                .withExpected(expected);

        client.updateItem(updateEntry);
    }

    private Map<String, ExpectedAttributeValue> expectNotExists() {
        Map<String, ExpectedAttributeValue> expected = new HashMap<>();
        expected.put(partitionKeyName.toString(), new ExpectedAttributeValue(false));
        return expected;
    }

    private Map<String, ExpectedAttributeValue> expectExists(Entry entry) {
        Map<String, ExpectedAttributeValue> expected = new HashMap<>();

        ExpectedAttributeValue expectedAttributeValue = new ExpectedAttributeValue(true);
        expectedAttributeValue.setValue(new AttributeValue(getPartitionKeyValue(entry)));
        expected.put(partitionKeyName.toString(), expectedAttributeValue);

        // FIXME: hardcode whole file, or make generic
        ExpectedAttributeValue expectedSha = new ExpectedAttributeValue(true);
        expectedSha.setValue(new AttributeValue(sha(entry)));
        expected.put(OPTIMISTIC_LOCK_FIELD_NAME, expectedSha);

        return expected;
    }

    private String sha(Entry entry) {
        RawSecretEntry rse = (RawSecretEntry) entry;
        return Encoder.base64encode(rse.sha1OfEncryptionPayload());
    }

    private Map<String, AttributeValueUpdate> createAttributes(Entry entry) {
        Map<String, AttributeValueUpdate> attributes = new HashMap<>();
        attributes.put(SCHEMA_VERSION_FIELD_NAME, new AttributeValueUpdate()
                .withAction(AttributeAction.PUT)
                .withValue(new AttributeValue().withN(SCHEMA_VERSION)));

        attributes.put(OPTIMISTIC_LOCK_FIELD_NAME, new AttributeValueUpdate()
                .withAction(AttributeAction.PUT)
                .withValue(new AttributeValue().withS(sha(entry))));

        for (Map.Entry<Integer, String> e : attributeMappings.entrySet()) {

            Object value = getValue(entry, e.getValue());
            if (value != null) {
                attributes.put(e.getKey().toString(),
                        new AttributeValueUpdate()
                                .withAction(AttributeAction.PUT)
                                .withValue(getAttribute(value)));
            }
        }
        return attributes;
    }

    private AttributeValue getAttribute(Object value) {
        if (value instanceof String) {
            return new AttributeValue().withS((String)value);
        } else if (value instanceof Byte || value instanceof Long) {
            return new AttributeValue().withN(value.toString());
        } else if (value instanceof byte[]) {
            return new AttributeValue().withS(Encoder.base64encode((byte[])value));
        } else {
            throw new RuntimeException("Failed to recognize type");
        }
    }

    private Map<String, AttributeValue> createKey(Entry entry) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(partitionKeyName.toString(), new AttributeValue().withS(getPartitionKeyValue(entry)));
        // TODO: do not assume number sortKey?
        key.put(sortKeyName.toString(), new AttributeValue().withN(getSortKeyValue(entry)));
        return key;
    }

    private String getPartitionKeyValue(Entry entry) {
        return getValue(entry, partitionKeyMapping.get(partitionKeyName)).toString();
    }

    private Primary getUnconvertedPartitionKeyValue(Entry entry) {
        Field field = getField(entry, partitionKeyMapping.get(partitionKeyName));
        try {
            return (Primary) field.get(entry);
        } catch (IllegalAccessException e) {
            throw new FieldAccessException(field.getName(), entry.getClass().getName(), e);
        }
    }

    private String getSortKeyValue(Entry entry) {
        return getValue(entry, sortKeyMapping.get(sortKeyName)).toString();
    }

    private Object getValue(Entry entry, String fieldName) {
        try {
            Field field = getField(entry, fieldName);
            return converters.toObject(field.get(entry));
        } catch (IllegalAccessException e) {
            throw new FieldAccessException(fieldName, entry.getClass().getName(), e);
        }
    }

    private Field getField(Entry entry, String fieldName) {
        try {
            return entry.getClass().getField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new FieldAccessException(fieldName, entry.getClass().getName(), e);
        }
    }

    private String getFieldName(String attributeName) {
        if (attributeName.equals(partitionKeyName.toString())) {
            return partitionKeyMapping.get(partitionKeyName);
        } else if (attributeName.endsWith(sortKeyName.toString())) {
            return sortKeyMapping.get(sortKeyName);
        } else {
            return attributeMappings.get(Integer.valueOf(attributeName));
        }
    }

    @Override
    public void delete(Primary partitionKeyValue) {
        readWriteLock.writeLock().lock();

        try {
            stream()
                    .filter(partitionKeyRef.eq(partitionKeyValue))
                    .toJavaStream()
                    .forEach(entry -> client.deleteItem(tableName, createKey(entry)));
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public Set<Primary> keySet() {
        readWriteLock.readLock().lock();

        try {
            return stream()
                    .uniquePrimaryKey()
                    .toJavaStream()
                    .map(this::getUnconvertedPartitionKeyValue)
                    .collect(Collectors.toSet());
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public KVStream<Entry> stream() {
        return new KVStream<>(new DynamoDBExecutor(readWriteLock));
    }

    @Override
    public void close() {
        // Do nothing
    }

    private class DynamoDBExecutor implements SecretEventStream.Executor<Entry> {
        private final ReadWriteLock readWriteLock;

        DynamoDBExecutor(ReadWriteLock readWriteLock) {
            this.readWriteLock = readWriteLock;
        }

        @Override
        public Stream<Entry> toJavaStream(SecretEventStream.Filter<Entry> filter) {
            readWriteLock.readLock().lock();

            try {
                Stream<Entry> s = stream(filter);

                // TODO: performance? If we do not look at all the elements at this point, we might throw away the ones that are malicious without knowing later
                List<Entry> copy = s.collect(Collectors.toList());

                // Perform local checks to verify that the server is not visibly misbehaving
                if (filter.parsedKeyCondition.isPresent()) {
                    copy.forEach(entry -> {
                        if (!filter.parsedKeyCondition.get().evaluate(entry)) {
                            throw new PotentiallyMaliciousDataException(
                                    "The data returned from the server does not match the search expression!");
                        }});
                }

                if (filter.parsedAttributeCondition.isPresent()) {
                    copy.forEach(entry -> {
                        if (!filter.parsedAttributeCondition.get().evaluate(entry)) {
                            throw new PotentiallyMaliciousDataException(
                                    "The data returned from the server does not match the search expression!");
                        }});
                }


                Stream<Entry> result = copy.stream();

                if (filter.unique) {
                    result = result.filter(distinctByKey(e -> getPartitionKeyValue(e)));
                }

                return result;
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        private <T> Predicate<T> distinctByKey(Function<? super T,Object> keyExtractor) {
            Map<Object,Boolean> seen = new ConcurrentHashMap<>();
            return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
        }

        private Stream<Entry> stream(SecretEventStream.Filter<Entry> filter) {
            if (filter.parsedKeyCondition.isPresent()) {
                return query(filter);
            } else if (filter.parsedAttributeCondition.isPresent()) {
                return scan(filter, converters);
            } else {
                return all();
            }
        }

        private Stream<Entry> query(SecretEventStream.Filter<Entry> filter) {
            QueryRequest queryRequest = new QueryRequest();
            queryRequest.withTableName(tableName);
            queryRequest.withConsistentRead(true);

            if (filter.reverse) {
                queryRequest.setScanIndexForward(false);
            }

            KeyExpressionGenerator keyExpressionGenerator = new KeyExpressionGenerator();

            KeyExpressionGenerator.KeyCondition keyExpression = keyExpressionGenerator.process(filter.parsedKeyCondition.get(), converters);
            Map<String, String> expressionAttributeNames = keyExpression.expressionAttributeNames;
            Map<String, AttributeValue> expressionAttributeValues = keyExpression.expressionAttributeValues;

            if (filter.parsedAttributeCondition.isPresent()) {
                FilterGenerator filterGenerator = new FilterGenerator(expressionAttributeValues.size()+1);
                FilterGenerator.Filter generated = filterGenerator.process(filter.parsedAttributeCondition.get(), converters);

                if(!generated.expressionAttributeNames.isEmpty()) {
                    expressionAttributeNames = FilterGenerator.merge(expressionAttributeNames, generated.expressionAttributeNames);
                }

                if (!generated.expressionAttributeValues.isEmpty()) {
                    expressionAttributeValues = FilterGenerator.merge(expressionAttributeValues, generated.expressionAttributeValues);
                }

                queryRequest.withFilterExpression(generated.filterExpression);
            }

            queryRequest.withExpressionAttributeNames(expressionAttributeNames);
            queryRequest.withExpressionAttributeValues(expressionAttributeValues);
            queryRequest.withKeyConditionExpression(keyExpression.keyConditionExpression);

            QueryResult result = client.query(queryRequest);

            List<Map<String, AttributeValue>> results = new ArrayList<>();

            results.addAll(result.getItems());

            while (result.getLastEvaluatedKey() != null) {
                queryRequest = queryRequest.withExclusiveStartKey(result.getLastEvaluatedKey());

                result = client.query(queryRequest);

                results.addAll(result.getItems());
            }

            return results.stream().map(this::fromMap);
        }

        private Stream<Entry> scan(SecretEventStream.Filter<Entry> filter, Converters converters) {
            ScanRequest scanRequest = new ScanRequest();
            scanRequest.withConsistentRead(true);
            scanRequest.withTableName(tableName);

            FilterGenerator filterGenerator = new FilterGenerator();
            FilterGenerator.Filter generated = filterGenerator.process(filter.parsedAttributeCondition.get(), converters);

            if(!generated.expressionAttributeNames.isEmpty()) {
                scanRequest.withExpressionAttributeNames(generated.expressionAttributeNames);
            }

            if (!generated.expressionAttributeValues.isEmpty()) {
                scanRequest.withExpressionAttributeValues(generated.expressionAttributeValues);
            }

            scanRequest.withFilterExpression(generated.filterExpression);

            ScanResult result = client.scan(scanRequest);

            List<Map<String, AttributeValue>> results = new ArrayList<>();
            results.addAll(result.getItems());

            while (result.getLastEvaluatedKey() != null) {
                scanRequest = scanRequest.withExclusiveStartKey(result.getLastEvaluatedKey());

                result = client.scan(scanRequest);

                results.addAll(result.getItems());
            }

            Stream<Entry> typedResult = results.stream().map(this::fromMap);

            if (filter.reverse) {
                typedResult = Lists.reverse(typedResult.collect(Collectors.toCollection(LinkedList::new))).stream();
            }

            return typedResult;
        }

        private Stream<Entry> all() {
            ScanRequest scanRequest = new ScanRequest();
            scanRequest.withConsistentRead(true);
            scanRequest.withTableName(tableName);

            ScanResult result = client.scan(scanRequest);

            List<Map<String, AttributeValue>> results = new ArrayList<>();
            results.addAll(result.getItems());

            while (result.getLastEvaluatedKey() != null) {
                scanRequest = scanRequest.withExclusiveStartKey(result.getLastEvaluatedKey());

                result = client.scan(scanRequest);

                results.addAll(result.getItems());
            }


            return results.stream().map(this::fromMap);
        }

        private Entry fromMap(Map<String, AttributeValue> map) {
            String optimisticLock = "";

            try {
                Entry result = clazz.newInstance();

                // TODO: need to loop over spec, not what was returned
                for (Map.Entry<String, AttributeValue> entry : map.entrySet()) {
                    if (entry.getKey().equals(SCHEMA_VERSION_FIELD_NAME)) {
                        if (!entry.getValue().getN().equals(SCHEMA_VERSION)) {
                            throw new IllegalArgumentException(String.format("Expected version %s got version %s", SCHEMA_VERSION, entry.getValue().getS()));
                        }
                        continue;
                    }
                    if (entry.getKey().equals(OPTIMISTIC_LOCK_FIELD_NAME)) {
                        optimisticLock = entry.getValue().getS();
                        continue;
                    }

                    String fieldName = getFieldName(entry.getKey());
                    try {

                        Field field = clazz.getField(fieldName);

                        AttributeValue v = entry.getValue();

                        String value = getValueAsString(v);

                        Object fieldValue;
                        if (field.getType().equals(Optional.class)) {
                            ParameterizedType type = (ParameterizedType) field.getGenericType();
                            Class<?> unpackedFieldType = (Class<?>) type.getActualTypeArguments()[0];
                            fieldValue = getFieldValue(value, unpackedFieldType, true);
                        } else {
                            fieldValue = getFieldValue(value, field.getType(), false);
                        }

                        field.set(result, fieldValue);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new FieldAccessException(fieldName, entry.getClass().getName(), e);
                    }
                }
                verify(result, optimisticLock);
                return result;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new ParseException("Failed to instantiate class", e);
            }

        }

        void verify(Entry entry, String optimisticLock) {
            if (!sha(entry).equals(optimisticLock)) {
                throw new PotentiallyMaliciousDataException("The optimistic lock does not match the SHA1 of the encrypted payload");
            }
        }

        private Object getFieldValue(Object value, Class<?> type, boolean isOptional) {
            Class<?> clz = converters.getConvertedType(type);

            Object v;
            if (clz.equals(String.class)) {
                v = value;
            } else if (clz.equals(Long.class)) {
                v = Long.valueOf((String)value);
            } else if (clz.equals(Byte.class)) {
                v = Byte.valueOf((String) value);
            } else if (clz.equals(byte[].class)) {
                v = Encoder.base64decode((String)value);
            } else {
                throw new IllegalArgumentException(String.format("Unrecognized type '%s'", type.getName()));
            }

            if (isOptional) {
                return converters.fromOptionalObject(v, type);
            } else {
                return converters.fromObject(v, type);
            }
        }

        private String getValueAsString(AttributeValue attributeValue) {
            if (attributeValue.getS() != null) {
                return attributeValue.getS();
            } else if (attributeValue.getN() != null) {
                return attributeValue.getN();
            } else {
                throw new ParseException("Attribute value could not be extracted.");
            }
        }
    }
}
