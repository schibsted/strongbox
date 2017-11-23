/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.stream.file;

import com.schibsted.security.strongbox.sdk.exceptions.UnexpectedStateException;
import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;
import com.schibsted.security.strongbox.sdk.internal.encryption.EncryptionContext;
import com.schibsted.security.strongbox.sdk.internal.encryption.Encryptor;
import com.schibsted.security.strongbox.sdk.internal.encryption.FileEncryptionContext;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generated.File;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.backend.file.GenericFile;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend.RSEF;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import com.schibsted.security.strongbox.sdk.types.State;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.name;
import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.notAfter;
import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.notBefore;
import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.version;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * @author stiankri
 */
public class FileTest {

    private static class DummyEncryptor implements Encryptor {

        @Override
        public String encrypt(String data, EncryptionContext context) {
            return null;
        }

        @Override
        public String decrypt(String data, EncryptionContext context) {
            return null;
        }

        @Override
        public byte[] encrypt(byte[] data, EncryptionContext context) {
            return data;
        }

        @Override
        public byte[] decrypt(byte[] data, EncryptionContext context) {
            return data;
        }
    }

    SecretsGroupIdentifier group = new SecretsGroupIdentifier(Region.EU_WEST_1, "my.group");

    @Test
    public void test() {
        File store = new File(new java.io.File("test.sbx"), new DummyEncryptor(), new FileEncryptionContext(group), new ReentrantReadWriteLock());
        if (store.exists()) {
            store.delete();
        }

        SecretIdentifier secretIdentifier1 = new SecretIdentifier("MySecret");
        long version1 = 1;
        State state1 = State.ENABLED;
        byte[] payload = Encoder.asUTF8("encryptedPayload");
        RawSecretEntry entry1 = new RawSecretEntry(secretIdentifier1, version1, state1, Optional.empty(), Optional.empty(), payload);

        SecretIdentifier secretIdentifier2 = new SecretIdentifier("MySecret2");
        long version2 = 2;
        Optional<ZonedDateTime> notBeforeValue = Optional.of(ZonedDateTime.now());
        RawSecretEntry entry2 = new RawSecretEntry(secretIdentifier2, version2, state1, notBeforeValue, Optional.empty(), payload);

        store.create(entry1);
        store.create(entry2);

        List<RawSecretEntry> attr = store.stream().filter(name.eq(secretIdentifier1))
                .filter(
                        notAfter.isPresent()
                                .AND(RSEF.NOT(notAfter.get().eq(notBefore.get())))
                                .OR(notAfter.get().eq(notBefore.get()))
                                .AND(notBefore.get().eq(notAfter.get()))
                        )
                .toList();
        List<RawSecretEntry> all = store.stream().toList();
        List<RawSecretEntry> entries = store.stream().filter(name.eq(secretIdentifier1)).toList();

        List<RawSecretEntry> versionLargerThan = store.stream().filter(name.eq(secretIdentifier1).AND(version.ge(version1))).toList();


        List<RawSecretEntry> isPresent = store.stream()
                .filter(notBefore.isNotPresent())
                .toList();

        GenericFile genericFile = (GenericFile) store;
        genericFile.close();
    }

    @Test
    public void file_try_with_resources() {
        String path = "test.sbx";
        SecretIdentifier secretIdentifier1 = new SecretIdentifier("MySecret");
        long version1 = 1;
        State state1 = State.ENABLED;
        byte[] payload = Encoder.asUTF8("encryptedPayload");
        RawSecretEntry entry1 = new RawSecretEntry(secretIdentifier1, version1, state1, Optional.empty(), Optional.empty(), payload);

        SecretIdentifier secretIdentifier2 = new SecretIdentifier("MySecret2");
        long version2 = 2;
        Optional<ZonedDateTime> notBeforeValue = Optional.of(ZonedDateTime.of(2016, 5, 4, 2, 0 ,0, 0, ZoneId.of("UTC")));
        RawSecretEntry entry2 = new RawSecretEntry(secretIdentifier2, version2, state1, notBeforeValue, Optional.empty(), payload);

        try (File store = new File(new java.io.File(path), new DummyEncryptor(), new FileEncryptionContext(group), new ReentrantReadWriteLock())) {
            if (store.exists()) {
                store.delete();
            }
            store.create(entry1);
            store.create(entry2);
        } // auto closeable should write the results to disk when exiting the try clause, and thus be readable in the next section

        try (File file = new File(new java.io.File(path), new DummyEncryptor(), new FileEncryptionContext(group), new ReentrantReadWriteLock())) {
            List<RawSecretEntry> list = file.stream().toList();
            boolean t = list.get(1).equals(entry2);
            assertThat(list, containsInAnyOrder(entry1, entry2));
        }

        java.io.File f = new java.io.File(path);
        if (!f.delete()) {
            throw new UnexpectedStateException(path, "EXISTS", "DELETED", "File store deletion failed");
        }
    }
}
