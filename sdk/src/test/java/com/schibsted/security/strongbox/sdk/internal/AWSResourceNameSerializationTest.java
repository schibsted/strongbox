/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class AWSResourceNameSerializationTest {
    @Test
    public void testDecodeSecretsGroupName() {
        String decoded = AWSResourceNameSerialization.decodeSecretsGroupName("test-group-1");
        assertEquals(decoded, "test.group.1");
    }

    @Test
    public void testEncodeSecretsGroupName() {
        String encoded = AWSResourceNameSerialization.encodeSecretsGroupName("test.group.1");
        assertEquals(encoded, "test-group-1");

        // Decode and then encode. Should get original back.
        assertEquals(AWSResourceNameSerialization.encodeSecretsGroupName(
                AWSResourceNameSerialization.decodeSecretsGroupName("test-group-1")), "test-group-1");
    }
}
