/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

import com.google.common.collect.ImmutableMap;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import com.schibsted.security.strongbox.sdk.types.State;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * The security context is intended to prevent tampering with ciphertexts in the storage layer,
 * e.g. overwrite the newest version of a secret with an older, compromised one.
 *
 * @author stiankri
 */
public class DefaultEncryptionContext implements EncryptionContext {
    public final SecretsGroupIdentifier groupIdentifier;
    public final SecretIdentifier secretIdentifier;
    public final Long secretVersion;
    public final State state;
    public final Optional<ZonedDateTime> notBefore;
    public final Optional<ZonedDateTime> notAfter;


    public DefaultEncryptionContext(SecretsGroupIdentifier groupIdentifier,
                                    SecretIdentifier secretIdentifier,
                                    long secretVersion,
                                    State state,
                                    Optional<ZonedDateTime> notBefore,
                                    Optional<ZonedDateTime> notAfter) {
        this.groupIdentifier = groupIdentifier;
        this.secretIdentifier = secretIdentifier;
        this.secretVersion = secretVersion;
        this.state = state;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
    }

    @Override
    public Map<String, String> toMap() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        // It would have been nice to collect the padding into a separate field to avoid
        // polluting the other fields, but AWS KMS does not allow empty strings as
        // the value.
        builder.put("0", Padding.padWithSpaces(groupIdentifier.region.getName(), 14));
        builder.put("1", Padding.padWithSpaces(groupIdentifier.name, 64));
        builder.put("2", Padding.padWithSpaces(secretIdentifier.name, 128));
        builder.put("3", Padding.padWithZeros(secretVersion));
        builder.put("4", Padding.singleDigit(state.asByte()));
        builder.put("5", Padding.isPresent(notBefore));
        builder.put("6", Padding.asOptionalString(notBefore));
        builder.put("7", Padding.isPresent(notAfter));
        builder.put("8", Padding.asOptionalString(notAfter));

        return builder.build();
    }
}
