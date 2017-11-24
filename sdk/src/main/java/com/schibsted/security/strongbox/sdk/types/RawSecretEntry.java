/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.schibsted.security.strongbox.sdk.internal.encryption.BestEffortShred;
import com.schibsted.security.strongbox.sdk.internal.encryption.BestEffortShredder;
import com.schibsted.security.strongbox.sdk.exceptions.ParseException;
import com.schibsted.security.strongbox.sdk.exceptions.SerializationException;
import com.schibsted.security.strongbox.sdk.internal.json.StrongboxModule;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.annotation.Attribute;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.annotation.PartitionKey;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.annotation.SortKey;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

/**
 * Expect this class to change in the future
 *
 * @author stiankri
 */
// TODO make immutable/hashcode/++
public final class RawSecretEntry implements BestEffortShred {
    @PartitionKey(position=Config.KEY, padding = 128)
    @JsonProperty("secretIdentifier")
    public SecretIdentifier secretIdentifier;

    @SortKey(position=Config.VERSION)
    @JsonProperty("version")
    public Long version;

    @Attribute(position=Config.STATE)
    @JsonProperty("state")
    public State state;

    @Attribute(position=Config.NOT_BEFORE)
    @JsonProperty("notBefore")
    public Optional<ZonedDateTime> notBefore = Optional.empty();

    @Attribute(position=Config.NOT_AFTER)
    @JsonProperty("notAfter")
    public Optional<ZonedDateTime> notAfter = Optional.empty();

    @Attribute(position=Config.VALUE)
    @JsonProperty("encryptedPayload")
    public byte[] encryptedPayload;

    private static ObjectMapper objectMapper = new ObjectMapper().registerModules(new Jdk8Module(), new StrongboxModule());

    // TODO: remove
    @Deprecated
    public RawSecretEntry() {

    }

    // FIXME: remove JSON dependency
    public RawSecretEntry(@JsonProperty("secretIdentifier") SecretIdentifier secretIdentifier,
                          @JsonProperty("version") long version,
                          @JsonProperty("state") State state,
                          @JsonProperty("notBefore") Optional<ZonedDateTime> notBefore,
                          @JsonProperty("notAfter") Optional<ZonedDateTime> notAfter,
                          @JsonProperty("encryptedPayload") byte[] encryptedPayload) {
        this.secretIdentifier = secretIdentifier;
        this.version = version;
        this.state = state;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.encryptedPayload = encryptedPayload;
    }

    public String toJsonBlob() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to serialize secret entry to JSON blob", e);
        }
    }

    public static RawSecretEntry fromJsonBlob(String jsonBlob) {
        try {
            return objectMapper.readValue(jsonBlob, RawSecretEntry.class);
        } catch (IOException e) {
            throw new ParseException("Failed to deserialize secret entry from JSON blob", e);
        }
    }

    public byte[] sha1OfEncryptionPayload() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
            messageDigest.update(encryptedPayload);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to compute sha1 of encryption payload", e);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("secretIdentifier", secretIdentifier)
                .add("version", version)
                .add("state", state)
                .add("notBefore", notBefore)
                .add("notAfter", notAfter)
                .add("encryptedPayload", encryptedPayload)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(secretIdentifier, version, state, notBefore, notAfter, encryptedPayload);
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof RawSecretEntry){
            final RawSecretEntry other = (RawSecretEntry) obj;
            return Objects.equal(secretIdentifier, other.secretIdentifier)
                    && Objects.equal(version, other.version)
                    && Objects.equal(state, other.state)
                    && Objects.equal(notBefore, other.notBefore)
                    && Objects.equal(notAfter, other.notAfter)
                    && Arrays.equals(encryptedPayload, other.encryptedPayload);
        } else {
            return false;
        }
    }

    @Override
    public void bestEffortShred() {
        BestEffortShredder.shred(encryptedPayload);

        // Strings are immutable
        secretIdentifier = null;
        version = 0L;
        state = State.ENABLED;

        // ZonedDateTime is immutable, so the best we can hope for is that the memory is freed and then overwritten soon
        notBefore = Optional.empty();
        notAfter = Optional.empty();
    }
}
