/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.archaius;

import com.schibsted.security.strongbox.sdk.SecretsGroup;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;

import java.util.function.Function;

/**
 * Holds the decrypted secret in-memory. The secret will only be decrypted once per time it changes.
 * This is useful if you do not want the overhead of just-in-time decryption.
 *
 * @author stiankri
 * @author zamzterz
 */
public class InMemoryPlaintextSecret extends InMemoryPlaintextSecretDerived<String> {
    public InMemoryPlaintextSecret(SecretsGroup secretsGroup, SecretIdentifier secretIdentifier) {
        super(secretsGroup, secretIdentifier, Function.identity());
    }
}
