/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.archaius;

import com.netflix.config.DynamicStringProperty;
import com.schibsted.security.strongbox.sdk.SecretsGroup;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;

import java.util.Optional;

/**
 * Holds the decrypted secret in-memory. The secret will only be decrypted once per time it changes.
 * This is useful if you do not want the overhead of just-in-time decryption.
 *
 * @author stiankri
 */
public class InMemoryPlaintextSecret extends DynamicStringProperty {
    private Optional<String> value = Optional.empty();
    private final SecretsGroup secretsGroup;
    private final SecretIdentifier secretIdentifier;

    public InMemoryPlaintextSecret(SecretsGroup secretsGroup, SecretIdentifier secretIdentifier) {
        // Please note: null is used as the default value, as it generally does not make sense to have a default secret
        super(secretsGroup.srn(secretIdentifier).toSrn(), null);
        this.secretsGroup = secretsGroup;
        this.secretIdentifier = secretIdentifier;
        addCallback(callback());
    }

    // TODO synchronize?
    Runnable callback() {
      return () -> {
          String jsonBlob = super.get();

          if (jsonBlob != null) {
              RawSecretEntry rawSecretEntry = RawSecretEntry.fromJsonBlob(jsonBlob);
              SecretEntry secretEntry = secretsGroup.decrypt(rawSecretEntry, secretIdentifier, rawSecretEntry.version);
              value = Optional.of(secretEntry.secretValue.asString());
              rawSecretEntry.bestEffortShred();
              secretEntry.bestEffortShred();
          } else {
              value = Optional.of(null);
          }
        };
    }

    /**
     * Get the secret kept in-memory.
     *
     * @return The decrypted secret as a {@code String}, or {@code null}
     */
    @Override
    public String get() {
        if (!value.isPresent()) {
            callback().run();
        }

        return value.get();
    }
}
