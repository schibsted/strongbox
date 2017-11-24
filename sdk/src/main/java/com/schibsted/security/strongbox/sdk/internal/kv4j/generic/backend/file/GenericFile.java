/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.kv4j.generic.backend.file;

import com.google.common.collect.Lists;
import com.schibsted.security.strongbox.sdk.internal.encryption.EncryptionContext;
import com.schibsted.security.strongbox.sdk.internal.encryption.Encryptor;
import com.schibsted.security.strongbox.sdk.exceptions.AlreadyExistsException;
import com.schibsted.security.strongbox.sdk.exceptions.DoesNotExistException;
import com.schibsted.security.strongbox.sdk.exceptions.FieldAccessException;
import com.schibsted.security.strongbox.sdk.exceptions.NoFieldMatchingAnnotationException;
import com.schibsted.security.strongbox.sdk.exceptions.ParseException;
import com.schibsted.security.strongbox.sdk.exceptions.SerializationException;
import com.schibsted.security.strongbox.sdk.exceptions.UnexpectedStateException;
import com.schibsted.security.strongbox.sdk.exceptions.UnsupportedTypeException;
import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.GenericStore;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.annotation.PartitionKey;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.annotation.Attribute;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.annotation.SortKey;
import com.schibsted.security.strongbox.sdk.internal.converter.Converters;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend.KVStream;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend.RSEF;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend.SecretEventStream;
import com.schibsted.security.strongbox.sdk.internal.interfaces.ManagedResource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manage files and store/retrieve data entries
 *
 * @author stiankri
 * @author kvlees
 */
public class GenericFile<Entry, Primary, Secondary extends Comparable<? super Secondary>> implements GenericStore<Entry, Primary>, AutoCloseable, ManagedResource {
    Map<Primary, Map<Secondary, Entry>> store = new HashMap<>();
    Converters converters;
    Class<Entry> clazz;
    File file;
    private final ReadWriteLock readWriteLock;
    private final byte SERIALIZATION_VERSION = 1;
    private final byte VERSION = 1;

    List<String> fieldNames = new ArrayList<>();
    Map<String, Integer> padding = new HashMap<>();

    private Encryptor encryptor;
    private EncryptionContext encryptionContext;

    public GenericFile(java.io.File path,
                       Converters converters,
                       Encryptor encryptor,
                       EncryptionContext encryptionContext,
                       Class<Entry> clazz,
                       ReadWriteLock readWriteLock) {
        this.file = path;
        this.converters = converters;
        this.clazz = clazz;
        this.readWriteLock = readWriteLock;
        this.encryptor = encryptor;
        this.encryptionContext = encryptionContext;
        buildMappings();

        open();
    }

    private void buildMappings() {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Attribute[] attributes = field.getAnnotationsByType(Attribute.class);
            PartitionKey[] partitionKey = field.getAnnotationsByType(PartitionKey.class);
            SortKey[] sortKey = field.getAnnotationsByType(SortKey.class);

            if (attributes.length > 0) {
                fieldNames.add(field.getName());
            }

            if (partitionKey.length > 0) {
                fieldNames.add(field.getName());
                padding.put(field.getName(), partitionKey[0].padding());
            }

            if (sortKey.length > 0) {
                fieldNames.add(field.getName());
            }
        }
    }

    public List<Entry> fromByteArray(byte[] payload) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(payload);

        byte version = byteBuffer.get();
        if (version != 1) {
            throw new IllegalArgumentException("The version needs to be 1");
        }

        long numEntries = byteBuffer.getLong();

        List<Entry> list = new ArrayList<>();

        for (long index = 0; index < numEntries; ++index) {

            byte schemaVersion = byteBuffer.get();
            if (schemaVersion != VERSION) {
                throw new IllegalArgumentException(String.format("The schema version should be %d but was %d", VERSION, schemaVersion));
            }

            try {
                Entry result = clazz.newInstance();
                for (String fieldName : fieldNames) {

                    Field field = clazz.getField(fieldName);
                    Object fieldValue = getFieldValue(byteBuffer, fieldName);
                    field.set(result, fieldValue);
                }

                list.add(result);
            } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException("Failed create Entry", e);
            }
        }
        return list;
    }

    private Class<?> getType(String fieldName) {
        try {
            Field field = clazz.getField(fieldName);
            return field.getType();
        } catch (NoSuchFieldException e) {
            throw new FieldAccessException(fieldName, clazz.getName());
        }
    }

    private Class<?> getTypeOfOptional(String fieldName) {
        try {
            Field field = clazz.getField(fieldName);
            ParameterizedType type = (ParameterizedType) field.getGenericType();
            return (Class<?>) type.getActualTypeArguments()[0];
        } catch (NoSuchFieldException e) {
            throw new FieldAccessException(fieldName, clazz.getName());
        }
    }

    private Object getFieldValue(ByteBuffer byteBuffer, String fieldName) {
        Class<?> clz = getConvertedType(fieldName);

        Object v = null;
        boolean present = true;
        if (isOptional(fieldName)) {
            byte p = byteBuffer.get();
            if (p == 0) {
                present = false;
            }
        }

        if (clz.equals(String.class)) {
            byte[] value = readArray(byteBuffer);
            v = Encoder.fromUTF8(value);
        } else if (clz.equals(Long.class)) {
            v = byteBuffer.getLong();
        } else if (clz.equals(Byte.class)) {
            v = byteBuffer.get();
        } else if (clz.equals(byte[].class)) {
            v = readArray(byteBuffer);
        } else {
            throw new IllegalArgumentException(String.format("Unrecognized type '%s'", clz.getName()));
        }

        if (!present) {
            v = null;
        }

        if (isOptional(fieldName)) {
            return converters.fromOptionalObject(v, getTypeOfOptional(fieldName));
        } else {
            return converters.fromObject(v, getType(fieldName));
        }
    }

    public byte[] toByteArray() {
        List<Entry> entries = stream().toList();
        Size size = computeLength(entries);

        int headerSize = 1 + 4 + 8;

        ByteBuffer byteBuffer = ByteBuffer.allocate(headerSize + size.totalSize);

        byteBuffer.put((byte)1); // version
        byteBuffer.putLong(entries.size()); // number of entries

        for (Entry entry : entries) {
            byteBuffer.put((byte)1); // version

            for (String fieldName : fieldNames) {
                Object value = getValue(entry, fieldName);
                Class<?> clz = getConvertedType(fieldName);
                int padding = getPadding(fieldName);

                if (isOptional(fieldName)) {
                    if (value == null) {
                        byteBuffer.put((byte)0);
                    } else {
                        byteBuffer.put((byte)1);
                    }
                }

                if (value == null) {
                        writeDummy(clz, byteBuffer, padding);
                } else {
                        write(clz, value, byteBuffer, padding);
                }
            }
        }

        byteBuffer.putInt(size.padding);
        byteBuffer.put(new byte[size.padding]);

        return byteBuffer.array();
    }

    private int getPadding(String fieldName) {
        Integer padding = this.padding.get(fieldName);
        if (padding != null) {
            return padding;
        } else {
            return 0;
        }
    }

    private void write(Class<?> type, Object value, ByteBuffer byteBuffer, int padding) {
        if (type.equals(String.class)) {
            byte[] v = Encoder.asUTF8((String)value);
            writeArray(byteBuffer, v);
        } else if (type.equals(Byte.class)) {
            byteBuffer.put((Byte)value);
        } else if (type.equals(Long.class)) {
            byteBuffer.putLong((Long)value);
        } else if (type.equals(byte[].class)) {
            byte[] v = (byte[])value;
            writeArray(byteBuffer, v);
        }
    }

    private void writeDummy(Class<?> type, ByteBuffer byteBuffer, int padding) {
        if (type.equals(String.class)) {
            writeArray(byteBuffer, new byte[0]);
        } else if (type.equals(Byte.class)) {
            byteBuffer.put((byte)0);
        } else if (type.equals(Long.class)) {
            byteBuffer.putLong(0);
        } else if (type.equals(byte[].class)) {
            writeArray(byteBuffer, new byte[0]);
        }
    }

    private void writeArray(ByteBuffer byteBuffer, byte[] value) {
        byteBuffer.putInt(value.length);
        byteBuffer.put(value);
    }

    private byte[] readArray(ByteBuffer byteBuffer) {
        int size = byteBuffer.getInt();
        byte[] value = new byte[size];
        byteBuffer.get(value);
        return value;
    }

    public static class Size {
        public int totalSize;
        public int padding;

        public Size(int totalSize, int padding) {
            this.totalSize = totalSize;
            this.padding = padding;
        }
    }

    private Size computeLength(Entry entry, String fieldName) {
        int padding = getPadding(fieldName);
        Class<?> type = getConvertedType(fieldName);
        int size = getBaseSize(type, entry, fieldName);

        int optional = (isOptional(fieldName)) ? 1 : 0;


        if (padding > 0) {
            if (type.equals(String.class)) {
                return new Size(padding + 4 + optional, padding-size);
            } else if (type.equals(Byte.class)) {
                return new Size(size + optional, 0);
            } else if (type.equals(Long.class)) {
                return new Size(size + optional, 0);
            } else if (type.equals(byte[].class)) {
                return new Size(padding + 4 + optional, padding - size);
            }
        } else {
            if (type.equals(String.class)) {
                return new Size(size + 4 + optional, 0);
            } else if (type.equals(Byte.class)) {
                return new Size(size + optional, 0);
            } else if (type.equals(Long.class)) {
                return new Size(size + optional, 0);
            } else if (type.equals(byte[].class)) {
                return new Size(size + 4 + optional, 0);
            }
        }

        throw new RuntimeException("illegal state");
    }

    private int getBaseSize(Class<?> type, Entry entry, String fieldName) {
        Object value = getValue(entry, fieldName);

        if (value != null) {
            if (type.equals(String.class)) {
                byte[] v = Encoder.asUTF8((String) value);
                return v.length;
            } else if (type.equals(Byte.class)) {
                return 1;
            } else if (type.equals(Long.class)) {
                return 8;
            } else if (type.equals(byte[].class)) {
                return ((byte[]) value).length;
            }
        } else {
            if (type.equals(String.class)) {
                return 0;
            } else if (type.equals(Byte.class)) {
                return 1;
            } else if (type.equals(Long.class)) {
                return 8;
            } else if (type.equals(byte[].class)) {
                return 0;
            }
        }

        throw new IllegalArgumentException("No suitable size");
    }

    private Size computeLength(List<Entry> entries) {
        int totalSize = 0;
        int padding = 0;

        for (Entry entry : entries) {
            totalSize++; // version
            for (String fieldName : fieldNames) {
                Size entrySize = computeLength(entry, fieldName);
                totalSize += entrySize.totalSize;
                padding += entrySize.padding;
            }
        }

        return new Size(totalSize, padding);
    }

    private Object getValue(Entry entry, String fieldName) {
        try {
            Field field = getField(entry, fieldName);
            return converters.toObject(field.get(entry));
        } catch (IllegalAccessException e) {
            throw new FieldAccessException(fieldName, entry.getClass().getName(), e);
        }
    }

    private boolean isOptional(String fieldName) {
        try {
            Field field = clazz.getField(fieldName);
            return field.getType().equals(Optional.class);
        } catch (NoSuchFieldException e) {
            throw new FieldAccessException(fieldName, clazz.getName());
        }
    }

    private Class<?> getConvertedType(String fieldName) {
        try {
            Field field = clazz.getField(fieldName);

            if (field.getType().equals(Optional.class)) {
                ParameterizedType type = (ParameterizedType) field.getGenericType();
                return converters.getConvertedType((Class<?>) type.getActualTypeArguments()[0]);
            } else {
                return converters.getConvertedType(field.getType());
            }
        } catch (NoSuchFieldException e) {
            throw new FieldAccessException(fieldName, clazz.getName());
        }

    }

    private Field getField(Entry entry, String fieldName) {
        try {
            return entry.getClass().getField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new FieldAccessException(fieldName, entry.getClass().getName(), e);
        }
    }

    @Override
    public String create() {
        readWriteLock.writeLock().lock();

        try {
            if (file.exists()) {
                throw new AlreadyExistsException(String.format("The file backend '%s' already exists", file.getAbsolutePath()));
            }
            close();

            return file.getAbsolutePath();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void delete() {
        readWriteLock.writeLock().lock();

        try {
            if (!file.exists()) {
                throw new DoesNotExistException(String.format("The file backend '%s' does not exists", file.getAbsolutePath()));
            }

            store = new HashMap<>();
            if (file.exists() && !file.delete()) {
                throw new UnexpectedStateException(file.getPath(), "EXISTS", "DELETED", "File store deletion failed");
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public Optional<String> awsAdminPolicy() {
        return Optional.empty();
    }

    @Override
    public Optional<String> awsReadOnlyPolicy() {
        return Optional.empty();
    }

    @Override
    public String getArn() {
        readWriteLock.readLock().lock();

        try {
            return file.getAbsolutePath();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public boolean exists() {
        readWriteLock.readLock().lock();

        try {
            return file.exists();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public void create(Entry entry) {
        readWriteLock.writeLock().lock();

        try {
            Primary name = getPartitionKey(entry);
            Secondary version = getSortKey(entry);

            if (!store.containsKey(name)) {
                store.put(name, new HashMap<>());
            }

            if (!store.get(name).containsKey(version)) {
                store.get(name).put(version, entry);
            } else {
                throw new AlreadyExistsException(String.format(
                        "File store entry already exists for name=%s,version=%s", name, version));
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void update(Entry entry, Entry existingEntry) {
        readWriteLock.writeLock().lock();

        try {
            Primary name = getPartitionKey(entry);
            Secondary version = getSortKey(entry);

            if (!store.containsKey(name) || store.get(name).containsKey(version)) {
                throw new DoesNotExistException(String.format(
                        "File store entry does not exist for: name=%s,version=%s,file=%s ", name, version, file.getPath()));
            } else {
                store.get(name).put(version, entry);
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    private void open() {
        try {
            if (file.exists()) {
                byte[] ciphertext = Files.readAllBytes(file.toPath());
                List<Entry> list = fromByteArray(encryptor.decrypt(verifyAndRemoveVersion(ciphertext), encryptionContext));
                list.forEach(this::create);
            }
        } catch (IOException e) {
            throw new ParseException("Failed to deserialize file: " + file.getPath(), e);
        }
    }

    @Override
    public void close() {
        readWriteLock.writeLock().lock();

        try {
            byte[] ciphertext = encryptor.encrypt(toByteArray(), encryptionContext);
            Files.write(file.toPath(), prependVersion(ciphertext));
        } catch (IOException e) {
            throw new SerializationException(String.format("Failed to serialize to file: '%s'", file.getPath()), e);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public byte[] prependVersion(byte[] data) {
        byte[] version = {SERIALIZATION_VERSION};

        byte[] versioned = new byte[data.length+1];

        System.arraycopy(version, 0, versioned, 0, version.length);
        System.arraycopy(data, 0, versioned, version.length, data.length);

        return versioned;
    }

    public byte[] verifyAndRemoveVersion(byte[] data) {
        if (data[0] != SERIALIZATION_VERSION) {
            throw new IllegalArgumentException(String.format("Version must be %d", SERIALIZATION_VERSION));
        }
        return Arrays.copyOfRange(data, 1, data.length);
    }

    @Override
    public void delete(Primary secretIdentifier) {
        readWriteLock.writeLock().lock();

        try {
            store.remove(secretIdentifier);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public Set<Primary> keySet() {
        readWriteLock.readLock().lock();

        try {
            return store.keySet();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public KVStream<Entry> stream() {
        return new KVStream<>(new FileExecutor(readWriteLock));
    }

    private class FileExecutor implements SecretEventStream.Executor<Entry> {
        private final ReadWriteLock readWriteLock;

        public FileExecutor(ReadWriteLock readWriteLock) {
            this.readWriteLock = readWriteLock;
        }

        @Override
        public Stream<Entry> toJavaStream(SecretEventStream.Filter<Entry> filter) {
            readWriteLock.readLock().lock();

            try {
                // TODO: optimize fetch record (i.e. both hash and range key is set to equal, nothing else)
                Stream<Entry> all = (filter.keyCondition.isPresent()) ? get(filter.parsedKeyCondition.get()) : all();

                if (filter.parsedAttributeCondition.isPresent()) {
                    RSEF.ParsedAttributeCondition<Entry> condition = filter.parsedAttributeCondition.get();
                    all = all.filter(condition::evaluate);
                }

                if (filter.reverse) {
                    all = Lists.reverse(all.collect(Collectors.toCollection(LinkedList::new))).stream();
                }

                return all;
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        private Stream<Entry> all() {
            return store.values().stream()
                    .flatMap(e -> e.values().stream());
        }

        Stream<Entry> get(RSEF.ParsedKeyCondition<Entry> keyCondition) {
            if (keyCondition instanceof RSEF.KeyAND) {
                RSEF.KeyAND<Entry> current = (RSEF.KeyAND<Entry>) keyCondition;
                RSEF.PartitionKeyEqualityOperator<Entry, Primary> e = (RSEF.PartitionKeyEqualityOperator<Entry, Primary>) current.left;

                // TODO extract version filter and iterate over
                Map<Secondary, Entry> f = store.get(e.right.value);

                RSEF.SortKeyComparisonOperator<Entry, Secondary> sortKeyComparisonOperator = (RSEF.SortKeyComparisonOperator<Entry, Secondary>) current.right;

                return f.values().stream().filter(sortKeyComparisonOperator::evaluate);
            } else if (keyCondition instanceof RSEF.PartitionKeyEqualityOperator) {
                RSEF.PartitionKeyEqualityOperator<Entry, Primary> current = (RSEF.PartitionKeyEqualityOperator<Entry, Primary>) keyCondition;
                return store.get(current.right.value).values().stream();
            } else {
                throw new UnsupportedTypeException(keyCondition.getClass().getName());
            }
        }
    }

    private Primary getPartitionKey(Entry entry) {
        Field[] fields = entry.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                PartitionKey[] partitionKey = field.getAnnotationsByType(PartitionKey.class);
                if (partitionKey.length > 0) {
                    return (Primary) field.get(entry);
                }
            } catch (IllegalAccessException e) {
                throw new FieldAccessException(field.getName(), entry.getClass().getName(), e);
            }
        }
        throw new NoFieldMatchingAnnotationException(PartitionKey.class.getName(), entry.getClass().getName());
    }

    private Secondary getSortKey(Entry entry) {
        Field[] fields = entry.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                SortKey[] sortKey = field.getAnnotationsByType(SortKey.class);
                if (sortKey.length > 0) {
                    return (Secondary) field.get(entry);
                }
            } catch (IllegalAccessException e) {
                throw new FieldAccessException(field.getName(), entry.getClass().getName(), e);
            }
        }
        throw new NoFieldMatchingAnnotationException(SortKey.class.getName(), entry.getClass().getName());
    }
}
