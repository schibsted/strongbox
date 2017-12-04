/*
 * Copyright (c) 2017 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.archaius;

import com.netflix.config.StringDerivedProperty;
import com.schibsted.security.strongbox.sdk.SecretsGroup;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;

import java.util.function.Function;

/**
 * A property wrapper that can be used to derive any type of data as property value
 * from an in-memory plaintext secret.
 *
 * @param <T> Type of the property value
 * @author zamzterz
 */
public class InMemoryPlaintextSecretDerived<T> extends StringDerivedProperty<T> {
    public InMemoryPlaintextSecretDerived(SecretsGroup secretsGroup, SecretIdentifier secretIdentifier, Function<String, T> decoder) {
        // Please note: null is used as the default value, as it generally does not make sense to have a default secret
        super(secretsGroup.srn(secretIdentifier).toSrn(),
                null,
                value -> decoder.apply(DecryptSecret.fromJsonBlob(value, secretsGroup, secretIdentifier)));
    }
}
