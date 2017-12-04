/*
 * Copyright (c) 2017 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.archaius;

import com.netflix.config.ConfigurationManager;
import com.schibsted.security.strongbox.sdk.SecretsGroup;
import com.schibsted.security.strongbox.sdk.internal.impl.DefaultSecretsGroup;
import com.schibsted.security.strongbox.sdk.types.SRN;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author zamzterz
 */
public class ArchaiusTestBase {
    protected final SecretIdentifier secretIdentifier = new SecretIdentifier("test_secret_name");
    protected SecretsGroup mockSecretsGroup;

    @BeforeMethod
    public void setUp() {
        mockSecretsGroup = mock(DefaultSecretsGroup.class);
        SRN mockSRN = mock(SRN.class);
        when(mockSRN.toSrn()).thenReturn(secretIdentifier.name);
        when(mockSecretsGroup.srn(secretIdentifier)).thenReturn(mockSRN);
    }

    @AfterMethod
    public void tearDown() {
        ConfigurationManager.getConfigInstance().clearProperty(secretIdentifier.name);
    }
}
