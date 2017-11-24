/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.kv4j.generic.backend.dynamodb;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.schibsted.security.strongbox.sdk.exceptions.AlreadyExistsException;
import com.schibsted.security.strongbox.sdk.exceptions.DoesNotExistException;
import com.schibsted.security.strongbox.sdk.exceptions.PotentiallyMaliciousDataException;
import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;
import com.schibsted.security.strongbox.sdk.internal.RegionLocalResourceName;
import com.schibsted.security.strongbox.sdk.internal.converter.FormattedTimestamp;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generated.DynamoDB;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import com.schibsted.security.strongbox.sdk.types.State;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
import static org.mockito.Mockito.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author kvlees
 * @author stiankri
 */
public class GenericDynamoDBTest {
    private static final String SECRET_GROUP_NAME = "test.group";
    private static final String SECRET_NAME = "secret1";
    private static final String SECRET2_NAME = "secret2";
    private static final String TEST_ARN = "arn:aws:arn:aws:dynamodb:us-west-1:1234:table/strongbox_us-west-1_test-group1";
    private static SecretsGroupIdentifier groupIdentifier = new SecretsGroupIdentifier(
            Region.US_WEST_1, SECRET_GROUP_NAME);
    private static String tableName = new RegionLocalResourceName(groupIdentifier).toString();

    // Constants for constructing AttributeValues
    private static final String SCHEMA_VERSION_FIELD_NAME = "0";
    private static final Integer KEY_ATTRIBUTE_NAME = 1;
    private static final Integer VERSION_ATTRIBUTE_NAME = 2;
    private static final Integer STATE_ATTRIBUTE_NAME = 3;
    private static final Integer NOT_BEFORE_ATTRIBUTE_NAME = 4;
    private static final Integer VALUE_ATTRIBUTE_NAME = 6;

    private static final String SCHEMA_VERSION = "1";

    private static final String OPTIMISTIC_LOCKING_ATTRIBUTE_NAME = "optimisticLock";

    private AmazonDynamoDBClient mockDynamoDBClient;
    private DynamoDB dynamoDB;

    @BeforeMethod
    public void setUp() throws Exception {
        this.mockDynamoDBClient = mock(AmazonDynamoDBClient.class);
        AWSCredentialsProvider mockCredentials = mock(AWSCredentialsProvider.class);
        ClientConfiguration mockConfig = mock(ClientConfiguration.class);
        this.dynamoDB = new DynamoDB(mockDynamoDBClient, mockCredentials, mockConfig, groupIdentifier, new ReentrantReadWriteLock());
    }

    private TableDescription constructTableDescription(TableStatus status) {
        return new TableDescription().withTableArn(TEST_ARN).withTableStatus(status);
    }

    private RawSecretEntry constructRawEntry(String secretName) {
        ZonedDateTime timestamp = ZonedDateTime.of(2016,6,1,13,37,42,0, ZoneId.of("UTC"));
        SecretIdentifier secretIdentifier = new SecretIdentifier(secretName);
        return new RawSecretEntry(secretIdentifier, 1, State.ENABLED, Optional.of(timestamp), Optional.empty(),
                Encoder.asUTF8("encrypted payload"));
    }

    private RawSecretEntry constructAlternativeRawSecretEntry(String secretName) {
        ZonedDateTime timestamp = ZonedDateTime.of(2015,6,1,13,37,42,0, ZoneId.of("UTC"));
        SecretIdentifier secretIdentifier = new SecretIdentifier(secretName);
        return new RawSecretEntry(secretIdentifier, 1, State.ENABLED, Optional.of(timestamp), Optional.empty(),
                Encoder.asUTF8("encrypted payload2"));
    }

    private UpdateItemRequest constructUpdateItemRequest(RawSecretEntry rawSecretEntry, boolean expectExists, Optional<RawSecretEntry> expectedRawSecretEntry) {
        // Create item key.
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(KEY_ATTRIBUTE_NAME.toString(), new AttributeValue().withS(rawSecretEntry.secretIdentifier.name));
        key.put(VERSION_ATTRIBUTE_NAME.toString(), new AttributeValue().withN(String.valueOf(rawSecretEntry.version)));


        // Create item attributes.
        Map<String, AttributeValueUpdate> attributes = new HashMap<>();
        attributes.put(SCHEMA_VERSION_FIELD_NAME,
                new AttributeValueUpdate()
                        .withAction(AttributeAction.PUT)
                        .withValue(new AttributeValue()
                                .withN(SCHEMA_VERSION)));

        attributes.put(NOT_BEFORE_ATTRIBUTE_NAME.toString(),
                new AttributeValueUpdate()
                        .withAction(AttributeAction.PUT)
                        .withValue(new AttributeValue()
                                .withN(FormattedTimestamp.epoch(rawSecretEntry.notBefore.get()).toString())));
        attributes.put(STATE_ATTRIBUTE_NAME.toString(),
                new AttributeValueUpdate()
                        .withAction(AttributeAction.PUT)
                        .withValue(new AttributeValue()
                                .withN(Byte.toString(rawSecretEntry.state.asByte()))));
        attributes.put(VALUE_ATTRIBUTE_NAME.toString(),
                new AttributeValueUpdate()
                        .withAction(AttributeAction.PUT)
                        .withValue(new AttributeValue()
                                .withS(Encoder.base64encode(rawSecretEntry.encryptedPayload))));
        attributes.put(OPTIMISTIC_LOCKING_ATTRIBUTE_NAME,
                new AttributeValueUpdate()
                        .withAction(AttributeAction.PUT)
                        .withValue(new AttributeValue()
                                .withS(Encoder.base64encode(rawSecretEntry.sha1OfEncryptionPayload()))));

        // Create the expected conditions map.
        Map<String, ExpectedAttributeValue> expected = new HashMap<>();
        if (expectExists) {
            expected.put(KEY_ATTRIBUTE_NAME.toString(), new ExpectedAttributeValue(true).withValue(
                    new AttributeValue(rawSecretEntry.secretIdentifier.name)));
            expected.put(OPTIMISTIC_LOCKING_ATTRIBUTE_NAME, new ExpectedAttributeValue(true).withValue(
                    new AttributeValue(Encoder.sha1(expectedRawSecretEntry.get().encryptedPayload))));
        } else {
            expected.put(KEY_ATTRIBUTE_NAME.toString(), new ExpectedAttributeValue(false));
        }

        return new UpdateItemRequest(tableName, key, attributes).withExpected(expected);
    }

    private static ArrayList<Map<String, AttributeValue>> constructItems(boolean includeSecret2) {
        ArrayList<Map<String, AttributeValue>> items = new ArrayList<>(3);


        byte[] value1 = Encoder.asUTF8("The encrypted payload");
        Map<String, AttributeValue> item1 =  new HashMap<>();
        item1.put(KEY_ATTRIBUTE_NAME.toString(), new AttributeValue().withS(SECRET_NAME));
        item1.put(VALUE_ATTRIBUTE_NAME.toString(), new AttributeValue().withS(Encoder.base64encode(value1)));
        item1.put(VERSION_ATTRIBUTE_NAME.toString(), new AttributeValue().withN("1"));
        item1.put(STATE_ATTRIBUTE_NAME.toString(), new AttributeValue().withS("2"));
        item1.put(OPTIMISTIC_LOCKING_ATTRIBUTE_NAME, new AttributeValue().withS(Encoder.sha1(value1)));
        items.add(item1);

        byte[] value1v2 = Encoder.asUTF8("The encrypted payload v2");
        Map<String, AttributeValue> item1v2 =  new HashMap<>();
        item1v2.put(KEY_ATTRIBUTE_NAME.toString(), new AttributeValue().withS(SECRET_NAME));
        item1v2.put(VALUE_ATTRIBUTE_NAME.toString(), new AttributeValue().withS(Encoder.base64encode(value1v2)));
        item1v2.put(VERSION_ATTRIBUTE_NAME.toString(), new AttributeValue().withN("2"));
        item1v2.put(STATE_ATTRIBUTE_NAME.toString(), new AttributeValue().withS("2"));
        item1v2.put(OPTIMISTIC_LOCKING_ATTRIBUTE_NAME, new AttributeValue().withS(Encoder.sha1(value1v2)));
        items.add(item1v2);

        if (includeSecret2) {
            byte[] value2 = Encoder.asUTF8("A different encrypted payload");
            Map<String, AttributeValue> item2 =  new HashMap<>();
            item2.put(KEY_ATTRIBUTE_NAME.toString(), new AttributeValue().withS(SECRET2_NAME));
            item2.put(VALUE_ATTRIBUTE_NAME.toString(), new AttributeValue().withS(Encoder.base64encode(value2)));
            item2.put(VERSION_ATTRIBUTE_NAME.toString(), new AttributeValue().withN("1"));
            item2.put(STATE_ATTRIBUTE_NAME.toString(), new AttributeValue().withS("2"));
            item2.put(OPTIMISTIC_LOCKING_ATTRIBUTE_NAME, new AttributeValue().withS(Encoder.sha1(value2)));
            items.add(item2);
        }
        return items;
    }

    private static ScanResult constructScanResult() {
        ArrayList<Map<String, AttributeValue>> items = constructItems(true);
        return new ScanResult().withItems(items).withCount(items.size()).withScannedCount(items.size());
    }

    private static QueryResult constructQueryResult(boolean includeSecret2) {
        ArrayList<Map<String, AttributeValue>> items = constructItems(includeSecret2);
        return new QueryResult().withItems(items).withCount(items.size()).withScannedCount(items.size());
    }

    private static Map<String, AttributeValue> constructKey(String secretName, int version) {
        Map<String, AttributeValue> key = new HashMap();
        key.put(KEY_ATTRIBUTE_NAME.toString(), new AttributeValue().withS(secretName));
        key.put(VERSION_ATTRIBUTE_NAME.toString(), new AttributeValue().withN(""+version));
        return key;
    }

    private static QueryRequest constructQueryRequest(String secretName) {
        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#1", KEY_ATTRIBUTE_NAME.toString());
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":a", new AttributeValue().withS(secretName));

        QueryRequest request = new QueryRequest()
                .withTableName(tableName)
                .withConsistentRead(true)
                .withKeyConditionExpression("#1 = :a")
                .withExpressionAttributeNames(expressionAttributeNames)
                .withExpressionAttributeValues(expressionAttributeValues);
        return request;
    }

    @Test
    public void testCreateTableWithWait() throws Exception {
        // Create fake responses from AWS. First response is still creating the table, second response the table
        // has become active.
        TableDescription creatingDescription = constructTableDescription(TableStatus.CREATING);
        TableDescription createdDescription = constructTableDescription(TableStatus.ACTIVE);
        CreateTableResult mockCreateResult = new CreateTableResult().withTableDescription(creatingDescription);
        DescribeTableResult mockDescribeResultCreating = new DescribeTableResult().withTable(creatingDescription);
        DescribeTableResult mockDescribeResultCreated = new DescribeTableResult().withTable(createdDescription);

        // Create the table.
        CreateTableRequest expectedRequest = dynamoDB.constructCreateTableRequest();
        when(mockDynamoDBClient.createTable(expectedRequest)).thenReturn(mockCreateResult);
        when(mockDynamoDBClient.describeTable(tableName)).thenReturn(mockDescribeResultCreating, mockDescribeResultCreated);
        assertEquals(dynamoDB.create(), TEST_ARN);

        verify(mockDynamoDBClient, times(1)).createTable(expectedRequest);
        verify(mockDynamoDBClient, times(2)).describeTable(tableName);
    }

    @Test
    public void testDeleteTableWithWait() throws Exception {
        // Create fake responses from AWS.
        TableDescription deletingDescription = constructTableDescription(TableStatus.DELETING);
        DescribeTableResult mockDescribeResult = new DescribeTableResult().withTable(deletingDescription);

        // Delete the table. First response the table is still deleting, the second response the table has deleted
        // and the ResourceNotFoundException is thrown.
        when(mockDynamoDBClient.describeTable(tableName)).thenReturn(mockDescribeResult).thenThrow(
                new ResourceNotFoundException("Table not found"));
        dynamoDB.delete();

        verify(mockDynamoDBClient, times(1)).deleteTable(tableName);
        verify(mockDynamoDBClient, times(2)).describeTable(tableName);
    }

    @Test
    public void testCreateEntry() throws Exception {
        RawSecretEntry rawSecretEntry =  constructRawEntry(SECRET_NAME);
        UpdateItemRequest expectedUpdateRequest = constructUpdateItemRequest(rawSecretEntry, false, Optional.empty());

        dynamoDB.create(rawSecretEntry);
        verify(mockDynamoDBClient, times(1)).updateItem(expectedUpdateRequest);
    }

    @Test
    public void testCreateEntryAlreadyExists() throws Exception {
        RawSecretEntry rawSecretEntry = constructRawEntry(SECRET_NAME);
        UpdateItemRequest expectedUpdateRequest = constructUpdateItemRequest(rawSecretEntry, false, Optional.empty());

        // Already exists will cause a check failed exception.
        when(mockDynamoDBClient.updateItem(expectedUpdateRequest)).thenThrow(
                new ConditionalCheckFailedException(""));

        boolean exceptionThrown = false;
        try {
            dynamoDB.create(rawSecretEntry);
        } catch (AlreadyExistsException e) {
            assertEquals(e.getMessage(), "DynamoDB store entry already exists:{1={S: secret1,}, 2={N: 1,}}");
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        verify(mockDynamoDBClient, times(1)).updateItem(expectedUpdateRequest);
    }

    @Test
    public void testUpdateEntry() throws Exception {
        RawSecretEntry rawSecretEntry = constructRawEntry(SECRET_NAME);
        RawSecretEntry alternativeRawSecretEntry = constructAlternativeRawSecretEntry(SECRET_NAME);

        UpdateItemRequest expectedUpdateRequest = constructUpdateItemRequest(rawSecretEntry, true, Optional.of(alternativeRawSecretEntry));

        dynamoDB.update(rawSecretEntry, alternativeRawSecretEntry);
        verify(mockDynamoDBClient, times(1)).updateItem(expectedUpdateRequest);
    }

    @Test
    public void testUpdateEntryDoesNotExist() throws Exception {
        RawSecretEntry rawSecretEntry = constructRawEntry(SECRET_NAME);
        RawSecretEntry alternativeRawSecretEntry = constructAlternativeRawSecretEntry(SECRET_NAME);

        UpdateItemRequest expectedUpdateRequest = constructUpdateItemRequest(rawSecretEntry, true, Optional.of(alternativeRawSecretEntry));

        when(mockDynamoDBClient.updateItem(expectedUpdateRequest)).thenThrow(
                new ConditionalCheckFailedException(""));

        boolean exceptionThrown = false;
        try {
            dynamoDB.update(rawSecretEntry, alternativeRawSecretEntry);
        } catch (DoesNotExistException e) {
            assertEquals(e.getMessage(), "Precondition to update entry in DynamoDB failed:{1={S: secret1,}, 2={N: 1,}}");
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        // Check all the expected calls to AWS were made.
        verify(mockDynamoDBClient, times(1)).updateItem(expectedUpdateRequest);
    }

    @Test
    public void testKeySet() throws Exception {
        ScanRequest request = new ScanRequest().withConsistentRead(true).withTableName(tableName);
        ScanResult result = constructScanResult();
        when(mockDynamoDBClient.scan(request)).thenReturn(result);

        // Call the KeySet method and assert the expected secret identifiers are returned.
        Set<SecretIdentifier> keys = dynamoDB.keySet();
        assertEquals(keys.size(), 2);
        assertTrue(keys.contains(new SecretIdentifier(SECRET_NAME)));
        assertTrue(keys.contains(new SecretIdentifier(SECRET2_NAME)));

        verify(mockDynamoDBClient, times(1)).scan(request);
    }

    @Test
    public void testKeySetEmpty() throws Exception {
        ScanRequest request = new ScanRequest().withConsistentRead(true).withTableName(tableName);
        ScanResult result = new ScanResult().withCount(0).withItems(new ArrayList<>());
        when(mockDynamoDBClient.scan(request)).thenReturn(result);

        // Call the KeySet method and check that is it empty.
        Set<SecretIdentifier> keySet = dynamoDB.keySet();
        assertTrue(keySet.isEmpty());
        verify(mockDynamoDBClient, times(1)).scan(request);
    }

    @Test
    public void testDeleteSecret() {
        QueryRequest request = constructQueryRequest(SECRET_NAME);
        QueryResult result = constructQueryResult(false);
        when(mockDynamoDBClient.query(request)).thenReturn(result);

        // Call the delete secret method.
        dynamoDB.delete(new SecretIdentifier(SECRET_NAME));

        // Verify only the entries matching the correct secret were deleted.
        verify(mockDynamoDBClient, times(1)).query(request);
        verify(mockDynamoDBClient, times(1)).deleteItem(tableName, constructKey(SECRET_NAME, 1));
        verify(mockDynamoDBClient, times(1)).deleteItem(tableName, constructKey(SECRET_NAME, 2));
        verify(mockDynamoDBClient, never()).deleteItem(tableName, constructKey(SECRET2_NAME, 1));
    }

    @Test
    public void testDeleteSecretMaliciousResults() {
        QueryRequest request = constructQueryRequest(SECRET_NAME);

        // Result contains entries that do no match the filter in the request (i.e. items for secret2). So it should be
        // considered malicious.
        QueryResult maliciousResult = constructQueryResult(true);
        when(mockDynamoDBClient.query(request)).thenReturn(maliciousResult);

        // Call the delete secret method.
        boolean consideredMalicious = false;
        try {
            dynamoDB.delete(new SecretIdentifier(SECRET_NAME));
        } catch (PotentiallyMaliciousDataException e) {
            consideredMalicious = true;
        }
        assertTrue(consideredMalicious);

        // Verify nothing was actually deleted because the malicious check failed.
        verify(mockDynamoDBClient, times(1)).query(request);
        verify(mockDynamoDBClient, never()).deleteItem(tableName, constructKey(SECRET_NAME, 1));
        verify(mockDynamoDBClient, never()).deleteItem(tableName, constructKey(SECRET_NAME, 2));
        verify(mockDynamoDBClient, never()).deleteItem(tableName, constructKey(SECRET2_NAME, 1));
    }

    @Test
    public void testDeleteSecretNotInTable() {
        QueryRequest request = constructQueryRequest(SECRET_NAME);
        QueryResult result = new QueryResult().withCount(0).withItems(new ArrayList<>());
        when(mockDynamoDBClient.query(request)).thenReturn(result);

        // Try deleting the secret. Method should complete without throwing exception but deleteItem will not be
        // called for any secrets.
        dynamoDB.delete(new SecretIdentifier(SECRET_NAME));
        verify(mockDynamoDBClient, times(1)).query(request);
        verify(mockDynamoDBClient, never()).deleteItem(tableName, constructKey(SECRET_NAME, 1));
    }
}
