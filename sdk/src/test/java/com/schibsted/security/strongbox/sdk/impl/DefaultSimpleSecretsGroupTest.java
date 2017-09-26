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

package com.schibsted.security.strongbox.sdk.impl;

import com.google.common.collect.ImmutableMap;
import com.schibsted.security.strongbox.sdk.SecretsGroup;
import com.schibsted.security.strongbox.sdk.exceptions.EncodingException;
import com.schibsted.security.strongbox.sdk.internal.impl.DefaultSecretsGroup;
import com.schibsted.security.strongbox.sdk.testing.SecretEntryMock;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretType;
import com.schibsted.security.strongbox.sdk.types.SecretValue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author stiankri
 */
public class DefaultSimpleSecretsGroupTest {
    SecretsGroup mockSecretsGroup;
    DefaultSimpleSecretsGroup simpleSecretsGroup;

    SecretIdentifier stringSecretIdentifier = new SecretIdentifier("mySecret1");
    SecretIdentifier binarySecretIdentifier = new SecretIdentifier("mySecret2");
    SecretIdentifier notPresent = new SecretIdentifier("mySecret3");

    String value1 = "1234";
    String value2 = "5678";
    byte[] value3 = new byte[]{1, 2, 3, 4};
    byte[] value4 = new byte[]{5, 6, 7, 8};
    long version = 2;

    @BeforeMethod
    public void setup() {
        mockSecretsGroup = mock(DefaultSecretsGroup.class);

        SecretEntryMock latestStringSecret = SecretEntryMock.builder().secretValue(value1).build();
        SecretEntryMock versionedStringSecret = SecretEntryMock.builder().secretValue(value2).build();

        SecretValue secretString = new SecretValue("secret1", SecretType.OPAQUE);
        SecretValue secretString2 = new SecretValue("secret2", SecretType.OPAQUE);
        SecretValue secretBinary = new SecretValue("binary".getBytes(), SecretType.OPAQUE);
        SecretValue secretBinary2 = new SecretValue("binary2".getBytes(), SecretType.OPAQUE);
        SecretEntryMock secretStringEntry = SecretEntryMock.builder().secretValue(secretString).secretIdentifier("secretString").build();
        SecretEntryMock secretStringEntry2 = SecretEntryMock.builder().secretValue(secretString2).secretIdentifier("secretString2").build();
        SecretEntryMock secretBinaryEntry = SecretEntryMock.builder().secretValue(secretBinary).secretIdentifier("secretBinary").build();
        SecretEntryMock secretBinaryEntry2 = SecretEntryMock.builder().secretValue(secretBinary2).secretIdentifier("secretBinary2").build();

        SecretEntryMock latestBinarySecret = SecretEntryMock.builder().secretValue(value3).build();
        SecretEntryMock versionedBinarySecret = SecretEntryMock.builder().secretValue(value4).build();

        doReturn(Optional.of(latestStringSecret)).when(mockSecretsGroup).getLatestActiveVersion(stringSecretIdentifier);
        doReturn(Optional.of(versionedStringSecret)).when(mockSecretsGroup).getActive(stringSecretIdentifier, version);
        doReturn(Optional.of(latestBinarySecret)).when(mockSecretsGroup).getLatestActiveVersion(binarySecretIdentifier);
        doReturn(Optional.of(versionedBinarySecret)).when(mockSecretsGroup).getActive(binarySecretIdentifier, version);
        doReturn(Arrays.asList(secretStringEntry, secretStringEntry2, secretBinaryEntry, secretBinaryEntry2)).when(mockSecretsGroup).getLatestActiveVersionOfAllSecrets();

        simpleSecretsGroup = new DefaultSimpleSecretsGroup(mockSecretsGroup);
    }

    @Test
    public void getAllStringSecrets() {
        Map<String, String> secrets = simpleSecretsGroup.getAllLatestActiveStringSecrets();
        assertThat(secrets, is(ImmutableMap.of("secretString", "secret1", "secretString2", "secret2")));
    }

    @Test
    public void getAllByteSecrets() {
        Map<String, byte[]> secrets = simpleSecretsGroup.getAllLatestActiveByteSecrets();
        assertThat(secrets.get("secretBinary"), is("binary".getBytes()));
        assertThat(secrets.get("secretBinary2"), is("binary2".getBytes()));
    }
    @Test
    public void getStringSecret() {
        Optional<String> result = simpleSecretsGroup.getStringSecret(stringSecretIdentifier);
        assertThat(result, is(Optional.of(value1)));
    }

    @Test
    public void versioned_getStringSecret() {
        Optional<String> result = simpleSecretsGroup.getStringSecret(stringSecretIdentifier, version);
        assertThat(result, is(Optional.of(value2)));
    }

    @Test
    public void getBinarySecret() {
        Optional<byte[]> result = simpleSecretsGroup.getBinarySecret(binarySecretIdentifier);
        assertThat(result, is(Optional.of(value3)));
    }

    @Test
    public void versioned_getBinarySecret() {
        Optional<byte[]> result = simpleSecretsGroup.getBinarySecret(binarySecretIdentifier, version);
        assertThat(result, is(Optional.of(value4)));
    }

    @Test
    public void not_present_getStringSecret() {
        Optional<String> result = simpleSecretsGroup.getStringSecret(notPresent);
        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void not_present_versioned_getStringSecret() {
        Optional<String> result = simpleSecretsGroup.getStringSecret(notPresent, version);
        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void not_present_getBinarySecret() {
        Optional<byte[]> result = simpleSecretsGroup.getBinarySecret(notPresent);
        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void not_present_versioned_getBinarySecret() {
        Optional<byte[]> result = simpleSecretsGroup.getBinarySecret(notPresent, version);
        assertThat(result, is(Optional.empty()));
    }

    @Test(expectedExceptions = EncodingException.class)
    public void getStringSecret_on_binary_secret() {
        simpleSecretsGroup.getStringSecret(binarySecretIdentifier);
    }

    @Test(expectedExceptions = EncodingException.class)
    public void getBinarySecret_on_string_secret() {
        simpleSecretsGroup.getBinarySecret(stringSecretIdentifier);
    }
}
