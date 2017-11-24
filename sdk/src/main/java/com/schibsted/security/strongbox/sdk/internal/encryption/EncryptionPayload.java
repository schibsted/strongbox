/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.schibsted.security.strongbox.sdk.exceptions.ParseException;
import com.schibsted.security.strongbox.sdk.exceptions.SerializationException;
import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;
import com.schibsted.security.strongbox.sdk.internal.converter.FormattedTimestamp;
import com.schibsted.security.strongbox.sdk.internal.json.StrongboxModule;
import com.schibsted.security.strongbox.sdk.types.Comment;
import com.schibsted.security.strongbox.sdk.types.Encoding;
import com.schibsted.security.strongbox.sdk.types.SecretType;
import com.schibsted.security.strongbox.sdk.types.SecretValue;
import com.schibsted.security.strongbox.sdk.types.State;
import com.schibsted.security.strongbox.sdk.types.UserAlias;
import com.schibsted.security.strongbox.sdk.types.UserData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author stiankri
 */
public class EncryptionPayload implements BestEffortShred {
    public final SecretValue value;
    public final Optional<UserData> userData;
    public final ZonedDateTime created;
    public final ZonedDateTime modified;
    public final Optional<UserAlias> createdBy;
    public final Optional<UserAlias> modifiedBy;
    public final Optional<Comment> comment;

    private static ObjectMapper objectMapper = new ObjectMapper().registerModules(new Jdk8Module(), new StrongboxModule());
    private final SecureRandom random;

    @JsonCreator
    public EncryptionPayload(@JsonProperty("value") SecretValue value,
                             @JsonProperty("userdata") Optional<UserData> userData,
                             @JsonProperty("created") ZonedDateTime created,
                             Optional<UserAlias> createdBy,
                             @JsonProperty("modified") ZonedDateTime modified,
                             Optional<UserAlias> modifiedBy,
                             @JsonProperty("comment") Optional<Comment> comment) {

        this.value = value;
        this.userData = userData;
        this.created = created;
        this.modified = modified;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
        this.comment = comment;

        try {
            this.random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to instantiate random number generator", e);
        }
    }

    public static byte[] computeSHA(State state, Optional<ZonedDateTime> notBefore, Optional<ZonedDateTime> notAfter) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            messageDigest.update(state.asByte());
            messageDigest.update(toByteArray(notBefore));
            messageDigest.update(toByteArray(notAfter));

            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new SerializationException("Failed to get SHA for encryption payload", e);
        }
    }

    private static byte[] toByteArray(Optional<ZonedDateTime> date) {
        ByteBuffer buffer = ByteBuffer.allocate(9);
        if (date.isPresent()) {
            buffer.put((byte)1);
            buffer.putLong(FormattedTimestamp.epoch(date.get()));
        } else {
            buffer.put((byte)0);
            buffer.putLong(0);
        }
        return buffer.array();
    }

    public static boolean verifyDataIntegrity(State state, Optional<ZonedDateTime> notBefore, Optional<ZonedDateTime> notAfter, byte[] sha) {
        return Arrays.equals(computeSHA(state, notBefore, notAfter), sha);
    }


    public byte[] toByteArray() {
        byte[] userData = extractByteArray(this.userData.map(UserData::asByteArray));
        byte[] comment = extractByteArray(this.comment.map(Comment::asByteArray));
        byte[] createdBy = extract(this.createdBy.map(a -> a.alias));
        byte[] updatedBy = extract(this.modifiedBy.map(a -> a.alias));

        int padding = computePadding(userData, comment, createdBy, updatedBy);
        int totalLength = computeLength(padding, userData, comment, createdBy, updatedBy);

        ByteBuffer byteBuffer = ByteBuffer.allocate(totalLength);

        byteBuffer.put((byte)1); // Version
        byteBuffer.putLong(FormattedTimestamp.epoch(this.created));
        byteBuffer.putLong(FormattedTimestamp.epoch(this.modified));

        putArray(byteBuffer, createdBy);
        putArray(byteBuffer, updatedBy);

        byteBuffer.put(this.value.encoding.asByte());
        byteBuffer.put(this.value.type.asByte());
        putArray(byteBuffer, this.value.asByteArray());

        putArray(byteBuffer, userData);
        putArray(byteBuffer, comment);
        putArray(byteBuffer, new byte[padding]);

        return byteBuffer.array();
    }

    public static EncryptionPayload fromByteArray(byte[] payload) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(payload);
        byte version  = byteBuffer.get();
        if (version != (byte)1) {
            throw new IllegalStateException(String.format("Expected version 1, got %d", version));
        }

        ZonedDateTime created = FormattedTimestamp.fromEpoch(byteBuffer.getLong());
        ZonedDateTime modified = FormattedTimestamp.fromEpoch(byteBuffer.getLong());

        Optional<UserAlias> createdBy = readOptionalString(byteBuffer).map(UserAlias::new);
        Optional<UserAlias> modifiedBy = readOptionalString(byteBuffer).map(UserAlias::new);

        Encoding encoding = Encoding.fromByte(byteBuffer.get());
        SecretType secretType = SecretType.fromByte(byteBuffer.get());

        SecretValue value = new SecretValue(readArray(byteBuffer), encoding, secretType);
        Optional<UserData> userData = readUserData(byteBuffer);
        Optional<Comment> comment = readComment(byteBuffer);

        return new EncryptionPayload(value, userData, created, createdBy, modified, modifiedBy, comment);
    }

    private static byte[] readArray(ByteBuffer byteBuffer) {
        int length = byteBuffer.getInt();
        byte[] array = new byte[length];
        byteBuffer.get(array, 0, length);
        return array;
    }

    private void putArray(ByteBuffer byteBuffer, byte[] value) {
        byteBuffer.putInt(value.length);
        byteBuffer.put(value);
    }

    private static Optional<UserData> readUserData(ByteBuffer byteBuffer) {
        byte[] value = readArray(byteBuffer);
        return value.length == 0 ? Optional.empty() : Optional.of(new UserData(value));
    }

    private static Optional<Comment> readComment(ByteBuffer byteBuffer) {
        byte[] value = readArray(byteBuffer);
        return value.length == 0 ? Optional.empty() : Optional.of(new Comment(value));
    }

    private static Optional<String> readOptionalString(ByteBuffer byteBuffer) {
        byte[] value = readArray(byteBuffer);
        return extractOptionalString(value);
    }

    private byte[] extract(Optional<String> optionalString) {
        return optionalString.isPresent() ? Encoder.asUTF8(optionalString.get()) : new byte[0];
    }

    private byte[] extractByteArray(Optional<byte[]> optionalByteArray) {
        return optionalByteArray.isPresent() ? optionalByteArray.get() : new byte[0];
    }

    private static Optional<String> extractOptionalString(byte[] value) {
        return value.length == 0 ? Optional.empty() : Optional.of(Encoder.fromUTF8(value));
    }

    private int computePadding(byte[] userData, byte[] comment, byte[] createdBy, byte[] updatedBy) {
        int padding = 0;

        padding += moduloPadding(this.value.asByteArray().length, 1000, 50000);
        padding += randomPadding(userData.length, 50000, 1000);
        padding += absolutePadding(comment.length, 1000);
        padding += absolutePadding(createdBy.length, 32);
        padding += absolutePadding(updatedBy.length, 32);

        return padding;
    }

    /**
     * Generate a secure random number of bytes to add as padding, while also doing bounds checking.
     *
     * This is intended to be a catch all attempt to obfuscate the exact length rather that hiding short lengths
     * (as in the modulo case). The best approach is always to use exact padding whenever possible.
     *
     * Please keep in mind that the current implementation can result in actualLength+padding > maxLength,
     * which might be helpful to an attacker. Any byte above the maxLength will be a waste, unless maxLength
     * is later extended, resulting in actualLength_old+padding_old < maxLength_new. As this is not entirely
     * unlikely we will keep the current implementation.
     *
     * @param actualLength length of array to be padded
     * @param maxLength max length of array to be padded
     * @param maxPadding maximum number of bytes to add as padding
     * @return the random number of bytes to add as padding
     */
    int randomPadding(int actualLength, int maxLength, int maxPadding) {
        throwIfAboveMax(actualLength, maxLength);
        return random.nextInt(maxPadding);
    }

    int moduloPadding(int val, int mod, int max) {
        throwIfAboveMax(val, max);
        return mod - (val % mod);
    }

    int absolutePadding(int val, int max) {
        throwIfAboveMax(val, max);
        return max - val;
    }

    void throwIfAboveMax(int val, int max) {
        if (val > max) {
            throw new IllegalStateException("Field is larger than expected");
        }
    }

    private int computeLength(int padding, byte[] userData, byte[] comment, byte[] createdBy, byte[] updatedBy) {
        return Byte.BYTES // version
                + Long.BYTES // created
                + Long.BYTES // modified
                + Integer.BYTES + createdBy.length
                + Integer.BYTES + updatedBy.length
                + 1 // encoding
                + 1 // secret type
                + Integer.BYTES + this.value.asByteArray().length
                + Integer.BYTES + userData.length
                + Integer.BYTES + comment.length
                + Integer.BYTES + padding;
    }


    public String toJsonBlob() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to serialize to JSON blob", e);
        }
    }

    public static EncryptionPayload fromJsonBlob(String jsonBlob) {
        try {
            return objectMapper.readValue(jsonBlob, EncryptionPayload.class);
        } catch (IOException e) {
            throw new ParseException("Failed to deserialize JSON blob", e);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", value)
                .add("userdata", userData)
                .add("created", created)
                .add("modified", modified)
                .add("comment", comment)
                .toString();
    }

    //FIXME
    @Override
    public int hashCode() {
        return Objects.hashCode(value, created, comment);
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof EncryptionPayload){
            final EncryptionPayload other = (EncryptionPayload) obj;
            return Objects.equal(value, other.value)
                    && Objects.equal(userData, other.userData)
                    && Objects.equal(created, other.created)
                    && Objects.equal(modified, other.modified)
                    && Objects.equal(comment, other.comment);
        } else {
            return false;
        }
    }

    @Override
    public void bestEffortShred() {
        value.bestEffortShred();
        userData.ifPresent(UserData::bestEffortShred);
        comment.ifPresent(Comment::bestEffortShred);
    }
}
