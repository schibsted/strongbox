/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.kv4j.generic.backend.file;

import com.schibsted.security.strongbox.sdk.internal.converter.FormattedTimestamp;
import com.schibsted.security.strongbox.sdk.internal.encryption.EncryptionContext;
import com.schibsted.security.strongbox.sdk.internal.encryption.Encryptor;
import com.schibsted.security.strongbox.sdk.internal.encryption.FileEncryptionContext;
import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generated.File;
import com.schibsted.security.strongbox.sdk.types.Encoding;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import com.schibsted.security.strongbox.sdk.types.State;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.schibsted.security.strongbox.sdk.internal.encryption.BinaryTestHelpers.assertByteArray;
import static com.schibsted.security.strongbox.sdk.internal.encryption.BinaryTestHelpers.assertInt;
import static com.schibsted.security.strongbox.sdk.internal.encryption.BinaryTestHelpers.assertLong;
import static com.schibsted.security.strongbox.sdk.internal.encryption.BinaryTestHelpers.assertString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static com.schibsted.security.strongbox.sdk.internal.encryption.BinaryTestHelpers.assertByte;

/**
 * @author stiankri
 */
public class GenericFileTest {

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

    java.io.File path = new java.io.File("test.sbx");
    SecretsGroupIdentifier group = new SecretsGroupIdentifier(Region.EU_WEST_1, "my.group");
    File file = new File(path, new DummyEncryptor(), new FileEncryptionContext(group), new ReentrantReadWriteLock());

    SecretIdentifier secretIdentifier = new SecretIdentifier("MySecret");
    long version = 1;
    State state = State.ENABLED;
    byte[] payload = Encoder.asUTF8("dummy payload");
    ZonedDateTime timestamp = ZonedDateTime.of(2016,6,1,13,37,42,0, ZoneId.of("UTC"));

    RawSecretEntry rawSecretEntry = new RawSecretEntry(secretIdentifier, version, state, Optional.of(timestamp), Optional.empty(), payload);

    @Test
    public void test() {
        if (path.exists()) {
            path.delete();
        }

        file.create(rawSecretEntry);
        byte[] serialized = file.toByteArray();

        verifyThatPayloadIsToSpec(ByteBuffer.wrap(serialized));

        byte[] completeFileFormat = file.prependVersion(serialized);
        ByteBuffer completeBuffer = ByteBuffer.wrap(completeFileFormat);
        verifyFileFormat(completeBuffer);
        verifyThatPayloadIsToSpec(completeBuffer);

        List<RawSecretEntry> list = file.fromByteArray(serialized);
        System.out.println(list);
        assertThat(list.get(0), is(rawSecretEntry));
    }

    void verifyFileFormat(ByteBuffer byteBuffer) {
        assertByte("version", (byte)1, byteBuffer);
    }

    void verifyThatPayloadIsToSpec(ByteBuffer byteBuffer) {

        assertByte("version", (byte)1, byteBuffer);
        assertLong("numberOfEntries", 1, byteBuffer);

        assertByte("schemaVersion", (byte)1, byteBuffer);

        assertInt("lengthOfSecretName", 8, byteBuffer);
        assertString("secretName", secretIdentifier.name, byteBuffer);

        assertLong("secretVersion", version, byteBuffer);
        assertByte("state", state.asByte(), byteBuffer);

        assertByte("notBeforePresent", (byte)1, byteBuffer);
        assertLong("notBefore", FormattedTimestamp.epoch(timestamp), byteBuffer);

        assertByte("notAfterPresent", (byte)0, byteBuffer);
        assertLong("notAfter", 0, byteBuffer);

        assertInt("lengthOfPayload", payload.length, byteBuffer);
        assertByteArray("payload", payload, byteBuffer);

        int paddingLength = computePadding(secretIdentifier);
        assertInt("lengthOfPadding", paddingLength, byteBuffer);
        assertByteArray("padding", new byte[paddingLength], byteBuffer);

        assertThat("fully consumed", byteBuffer.remaining(), is(0));
    }

    private int computePadding(SecretIdentifier secretIdentifier) {
        return 128-secretIdentifier.name.length();
    }
}
