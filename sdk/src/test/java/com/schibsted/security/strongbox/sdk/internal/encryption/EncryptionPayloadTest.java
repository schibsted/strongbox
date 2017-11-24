/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

import com.schibsted.security.strongbox.sdk.exceptions.ParseException;
import com.schibsted.security.strongbox.sdk.internal.converter.FormattedTimestamp;
import com.schibsted.security.strongbox.sdk.types.Comment;
import com.schibsted.security.strongbox.sdk.types.SecretType;
import com.schibsted.security.strongbox.sdk.types.SecretValue;
import com.schibsted.security.strongbox.sdk.types.State;
import com.schibsted.security.strongbox.sdk.types.UserAlias;
import com.schibsted.security.strongbox.sdk.types.UserData;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.schibsted.security.strongbox.sdk.internal.encryption.BinaryTestHelpers.assertByte;
import static com.schibsted.security.strongbox.sdk.internal.encryption.BinaryTestHelpers.assertByteArray;
import static com.schibsted.security.strongbox.sdk.internal.encryption.BinaryTestHelpers.assertInt;
import static com.schibsted.security.strongbox.sdk.internal.encryption.BinaryTestHelpers.assertIntInRange;
import static com.schibsted.security.strongbox.sdk.internal.encryption.BinaryTestHelpers.assertLong;
import static com.schibsted.security.strongbox.sdk.internal.encryption.BinaryTestHelpers.assertString;
import static com.schibsted.security.strongbox.sdk.internal.encryption.BinaryTestHelpers.moduloPadding;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.*;

/**
 * @author stiankri
 * @author kvlees
 */
public class EncryptionPayloadTest {
    SecretValue value = new SecretValue("secretValue", SecretType.OPAQUE);
    Optional<UserData> userData = Optional.of(new UserData("userdata".getBytes(StandardCharsets.UTF_8)));
    ZonedDateTime created = ZonedDateTime.of(2016,6,1,0,0,0,0, ZoneId.of("UTC"));
    ZonedDateTime modified = ZonedDateTime.of(2017,6,1,0,0,0,0, ZoneId.of("UTC"));
    Optional<ZonedDateTime> notAfter = Optional.of(ZonedDateTime.of(2016,7,1,0,0,0,0, ZoneId.of("UTC")));
    Optional<UserAlias> createdBy = Optional.of(new UserAlias("john.doe"));
    Optional<UserAlias> modifiedBy = Optional.empty();
    Optional<Comment> comment = Optional.of(new Comment("some comment"));

    EncryptionPayload encryptionPayload = new EncryptionPayload(value, userData, created, createdBy, modified,
            modifiedBy, comment);
    String blob = "{\"value\":\"c2VjcmV0VmFsdWU=\",\"created\":\"2016-06-01T00:00:00Z[UTC]\","+
                  "\"modified\":\"2017-06-01T00:00:00Z[UTC]\"," +
                  "\"comment\":\"some comment\",\"sha\":\"NaSfO4aF+QLg27yeTZWDtcW8C2rfBY9sZ0M0r2p4wLA=\",\"userData\":\"dXNlcmRhdGE=\"}";


    byte[] binaryBlob = new byte[3107];

    @BeforeClass
    public void setup() {
        byte[] binaryContent = {1, 0, 0, 0, 0, 87, 78, 37, -128, 0, 0, 0, 0, 89, 47, 89, 0, 0, 0, 0, 8, 106, 111, 104, 110, 46, 100, 111, 101, 0, 0, 0, 0, 1, 0, 0, 0, 0, 11, 115, 101, 99, 114, 101, 116, 86, 97, 108, 117, 101, 0, 0, 0, 8, 117, 115, 101, 114, 100, 97, 116, 97, 0, 0, 0, 12, 115, 111, 109, 101, 32, 99, 111, 109, 109, 101, 110, 116, 0, 0, 11, -47};
        System.arraycopy(binaryContent, 0, binaryBlob, 0, binaryContent.length);
    }

    // TODO: remove json serialization, when binary is ready
    @Test(enabled = false)
    public void testSerialize() {
        String serialized = encryptionPayload.toJsonBlob();
        assertThat(serialized, is(blob));
    }

    // TODO: remove json serialization, when binary is ready
    @Test(enabled = false)
    public void testDeserialize() {
        EncryptionPayload deserialized = EncryptionPayload.fromJsonBlob(blob);
        assertThat(deserialized, is(encryptionPayload));
    }

    @Test(expectedExceptions = ParseException.class)
    public void testDeserializeInvalidJson() {
        EncryptionPayload deserialized = EncryptionPayload.fromJsonBlob("{!@#$%^");
    }

    @Test void deserializeBinary() {
        EncryptionPayload deserializedPayload = EncryptionPayload.fromByteArray(binaryBlob);
        assertThat(deserializedPayload, is(encryptionPayload));
    }

    @Test
    public void checkSerializationWithSpec() {
        byte[] payload = encryptionPayload.toByteArray();

        ByteBuffer byteBuffer = ByteBuffer.wrap(payload);

        assertByte("version", (byte)1, byteBuffer);
        assertLong("created", FormattedTimestamp.epoch(created), byteBuffer);
        assertLong("modified", FormattedTimestamp.epoch(modified), byteBuffer);

        assertInt("lengthOfCreatedBy", createdBy.get().alias.length(), byteBuffer);
        assertString("createdBy", createdBy.get().alias, byteBuffer);

        assertInt("lengthOfModifiedBy", 0, byteBuffer);

        assertByte("encoding", (byte)1, byteBuffer);
        assertByte("type", (byte)0, byteBuffer);

        assertInt("lengthOfValue", value.asByteArray().length, byteBuffer);
        assertByteArray("value", value.asByteArray(), byteBuffer);

        assertInt("lengthOfUserData", userData.get().asByteArray().length, byteBuffer);
        assertByteArray("userData", userData.get().asByteArray(), byteBuffer);

        assertInt("lengthOfComment", comment.get().asString().length(), byteBuffer);
        assertString("comment", comment.get().asString(), byteBuffer);

        int basePadding = computePadding(value, comment.get(), createdBy.get().alias);
        int actualPadding = assertIntInRange("paddingLength", basePadding, basePadding+999, byteBuffer);
        assertByteArray("padding", new byte[actualPadding], byteBuffer);

        assertThat("fully consumed", byteBuffer.remaining(), is(0));
    }

    public int computePadding(SecretValue secretValue, Comment comment, String createdBy) {
        return moduloPadding(secretValue.asByteArray().length, 1000)
                + (1000 - comment.asByteArray().length)
                + (32 - createdBy.length())
                + (32); // modified by
    }

    @Test
    public void largeSecretPadding() {
        SecretValue value = new SecretValue(new byte[1500], SecretType.OPAQUE);
        EncryptionPayload largeSecret = new EncryptionPayload(value, userData, created, createdBy, modified, modifiedBy, comment);
        byte[] payload = largeSecret.toByteArray();

        EncryptionPayload deserialized = EncryptionPayload.fromByteArray(payload);
        assertThat(deserialized, is(largeSecret));
    }

    @Test
    public void testToString() {
        assertEquals(
                encryptionPayload.toString(),
                "EncryptionPayload{value=SecretValue{type=opaque, secretEncoding=utf8}, userdata=Optional[UserData{}], " +
                "created=2016-06-01T00:00Z[UTC], modified=2017-06-01T00:00Z[UTC], " +
                "comment=Optional[Comment{}]}");
    }

    @Test
    public void ensureNotCommutative() {
        byte[] sha1 = EncryptionPayload.computeSHA(State.ENABLED, notAfter, Optional.empty());
        byte[] sha2 = EncryptionPayload.computeSHA(State.ENABLED, Optional.empty(), notAfter);

        assertNotEquals(sha1, sha2, "Should not be able to reorder and have the same hash");
    }

    @Test
    public void testEquals() {
        EncryptionPayload samePayload = new EncryptionPayload(value, userData, created, createdBy, modified,
                modifiedBy, comment);
        assertTrue(encryptionPayload.equals(samePayload));

        // Different value
        EncryptionPayload differentPayload = new EncryptionPayload(new SecretValue("DifferentValue", SecretType.OPAQUE), userData, created,
                createdBy, modified, modifiedBy, comment);
        assertFalse(encryptionPayload.equals(differentPayload));

        // Different userdata
        differentPayload = new EncryptionPayload(value, Optional.of(new UserData("different userdata".getBytes(StandardCharsets.UTF_8))),
                created, createdBy, modified, modifiedBy, comment);
        assertFalse(encryptionPayload.equals(differentPayload));

        // Different created
        differentPayload = new EncryptionPayload(value, userData,
                ZonedDateTime.of(2017,12,1,0,0,0,0, ZoneId.of("UTC")),
                createdBy, modified, modifiedBy, comment);
        assertFalse(encryptionPayload.equals(differentPayload));

        // Different modified
        differentPayload = new EncryptionPayload(value, userData, created, createdBy,
                ZonedDateTime.of(2017,12,1,0,0,0,0, ZoneId.of("UTC")), modifiedBy, comment);
        assertFalse(encryptionPayload.equals(differentPayload));

        // Different comment
        differentPayload = new EncryptionPayload(value, userData, created, createdBy, modified, modifiedBy,
                Optional.of(new Comment("different comment")));
        assertFalse(encryptionPayload.equals(differentPayload));

        // Different object type.
        assertFalse(encryptionPayload.equals("some string value"));
    }
}
