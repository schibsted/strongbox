/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.archaius;

import com.netflix.config.DynamicStringProperty;
import com.schibsted.security.strongbox.sdk.SecretsGroup;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;

/**
 * Decrypts the secret every time {@link #get() get} is called. The intention is
 * to avoid having the plaintext in-memory, but there is no guarantees to allow
 * us to clear memory, and especially immutable {@code String}s in Java.
 *
 * @author stiankri
 */
public class JustInTimeDecryptedSecret extends DynamicStringProperty {
    private final SecretsGroup secretsGroup;
    private final SecretIdentifier secretIdentifier;

    public JustInTimeDecryptedSecret(SecretsGroup secretsGroup, SecretIdentifier secretIdentifier) {
        // Please note: null is used as the default value, as it generally does not make sense to have a default secret
        super(secretsGroup.srn(secretIdentifier).toSrn(), null);
        this.secretsGroup = secretsGroup;
        this.secretIdentifier = secretIdentifier;
    }

    /**
     * Decrypt and return the secret.
     *
     * @return The decrypted secret as a {@code String}, or {@code null}
     */
    @Override
    public String get() {
        String jsonBlob = super.get();
        if (jsonBlob != null) {
            RawSecretEntry rawSecretEntry = RawSecretEntry.fromJsonBlob(jsonBlob);
            SecretEntry secretEntry = secretsGroup.decrypt(rawSecretEntry, secretIdentifier, rawSecretEntry.version);

            String secret = secretEntry.secretValue.asString();

            rawSecretEntry.bestEffortShred();
            secretEntry.bestEffortShred();

            return secret;
        } else {
            return null;
        }
    }
}
