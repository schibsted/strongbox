/*
 * Copyright (c) 2017 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.archaius;

import com.schibsted.security.strongbox.sdk.SecretsGroup;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;

/**
 * Helper class for decrypting a raw secret value.
 *
 * @author zamzterz
 */
class DecryptSecret {
    static String fromJsonBlob(String jsonBlob, SecretsGroup secretsGroup, SecretIdentifier secretIdentifier) {
        if (jsonBlob == null) {
            return null;
        }

        RawSecretEntry rawSecretEntry = RawSecretEntry.fromJsonBlob(jsonBlob);
        SecretEntry secretEntry = secretsGroup.decrypt(rawSecretEntry, secretIdentifier, rawSecretEntry.version);

        String secret = secretEntry.secretValue.asString();

        rawSecretEntry.bestEffortShred();
        secretEntry.bestEffortShred();

        return secret;
    }
}
