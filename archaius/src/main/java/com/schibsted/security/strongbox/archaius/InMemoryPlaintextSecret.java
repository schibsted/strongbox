/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Schibsted Products & Technology AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
