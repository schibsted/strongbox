/*
 * Copyright (c) 2017 Schibsted Media Group. All rights reserved.
 */
package com.schibsted.security.strongbox.archaius;

import com.netflix.config.ConfigurationManager;
import com.schibsted.security.strongbox.sdk.internal.impl.DefaultSecretsGroup;
import com.schibsted.security.strongbox.sdk.testing.SecretEntryMock;
import com.schibsted.security.strongbox.sdk.types.*;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author zamzterz
 */
public class InMemoryPlaintextSecretDerivedTest {
    @Test
    public void testSecretIsDecryptedBeforeBeingPassedToDecoder() {
        String secretName = "test_secret_name";
        SecretIdentifier secretIdentifier = new SecretIdentifier(secretName);
        String secretValue = "test_secret_value";
        long version = 1;
        RawSecretEntry rawSecret = new RawSecretEntry(secretIdentifier,
                version,
                State.ENABLED,
                Optional.empty(),
                Optional.empty(),
                new SecretValue(secretValue, SecretType.OPAQUE).asByteArray());
        SecretEntry secretEntry = new SecretEntryMock.Builder().secretValue(secretValue).build();

        DefaultSecretsGroup secretsGroupMock = mock(DefaultSecretsGroup.class);
        SRN srnMock = mock(SRN.class);
        when(srnMock.toSrn()).thenReturn(secretName);
        when(secretsGroupMock.srn(secretIdentifier)).thenReturn(srnMock);
        when(secretsGroupMock.decrypt(rawSecret, secretIdentifier, version)).thenReturn(secretEntry);

        InMemoryPlaintextSecretDerived<Integer> p = new InMemoryPlaintextSecretDerived<>(secretsGroupMock,
                secretIdentifier,
                String::length);
        ConfigurationManager.getConfigInstance().setProperty(secretName, rawSecret.toJsonBlob());
        assertEquals(secretValue.length(), p.getValue().intValue());
    }
}
