/*
 * Copyright (c) 2017 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.archaius;

import com.netflix.config.ConfigurationManager;
import com.schibsted.security.strongbox.sdk.testing.SecretEntryMock;
import com.schibsted.security.strongbox.sdk.types.*;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author zamzterz
 */
public class InMemoryPlaintextSecretTest extends ArchaiusTestBase {
    @Test
    public void testMissingSecretResultsInNull() {
        InMemoryPlaintextSecret p = new InMemoryPlaintextSecret(mockSecretsGroup, secretIdentifier);
        assertNull(p.getValue());
    }

    @Test
    public void testSecretIsDecrypted() {
        String secretValue = "test_secret_value";
        long version = 1;
        RawSecretEntry rawSecret = new RawSecretEntry(secretIdentifier,
                version,
                State.ENABLED,
                Optional.empty(),
                Optional.empty(),
                new SecretValue(secretValue, SecretType.OPAQUE).asByteArray());
        SecretEntry secretEntry = new SecretEntryMock.Builder().secretValue(secretValue).build();
        when(mockSecretsGroup.decrypt(rawSecret, secretIdentifier, version)).thenReturn(secretEntry);

        InMemoryPlaintextSecret p = new InMemoryPlaintextSecret(mockSecretsGroup, secretIdentifier);
        ConfigurationManager.getConfigInstance().setProperty(secretIdentifier.name, rawSecret.toJsonBlob());
        assertEquals(p.getValue(), secretValue);
    }
}
