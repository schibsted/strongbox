/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.impl;

import com.schibsted.security.strongbox.sdk.SecretsGroup;
import com.schibsted.security.strongbox.sdk.exceptions.EncodingException;
import com.schibsted.security.strongbox.sdk.internal.impl.DefaultSecretsGroup;
import com.schibsted.security.strongbox.sdk.testing.SecretEntryMock;
import com.schibsted.security.strongbox.sdk.types.ByteSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretType;
import com.schibsted.security.strongbox.sdk.types.SecretValue;
import com.schibsted.security.strongbox.sdk.types.StringSecretEntry;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
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

    SecretIdentifier stringSecretIdentifier2 = new SecretIdentifier("stringSecretIdentifier2");
    SecretIdentifier stringSecretIdentifier3 = new SecretIdentifier("stringSecretIdentifier3");
    SecretIdentifier binarySecretIdentifier2 = new SecretIdentifier("binarySecretIdentifier2");
    SecretIdentifier binarySecretIdentifier3 = new SecretIdentifier("binarySecretIdentifier3");

    SecretValue stringSecretValue2 = new SecretValue("secret2", SecretType.OPAQUE);
    SecretValue stringSecretValue3 = new SecretValue("secret3", SecretType.OPAQUE);
    SecretValue binarySecretValue2 = new SecretValue("binary2".getBytes(), SecretType.OPAQUE);
    SecretValue binarySecretValue3 = new SecretValue("binary3".getBytes(), SecretType.OPAQUE);

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

        SecretEntryMock secretStringEntry2 = SecretEntryMock.builder().secretValue(stringSecretValue2).version(1l).secretIdentifier(stringSecretIdentifier2).build();
        SecretEntryMock secretStringEntry3 = SecretEntryMock.builder().secretValue(stringSecretValue3).version(1l).secretIdentifier(stringSecretIdentifier3).build();
        SecretEntryMock secretBinaryEntry2 = SecretEntryMock.builder().secretValue(binarySecretValue2).version(1l).secretIdentifier(binarySecretIdentifier2).build();
        SecretEntryMock secretBinaryEntry3 = SecretEntryMock.builder().secretValue(binarySecretValue3).version(1l).secretIdentifier(binarySecretIdentifier3).build();

        SecretEntryMock latestBinarySecret = SecretEntryMock.builder().secretValue(value3).build();
        SecretEntryMock versionedBinarySecret = SecretEntryMock.builder().secretValue(value4).build();

        doReturn(Optional.of(latestStringSecret)).when(mockSecretsGroup).getLatestActiveVersion(stringSecretIdentifier);
        doReturn(Optional.of(versionedStringSecret)).when(mockSecretsGroup).getActive(stringSecretIdentifier, version);
        doReturn(Optional.of(latestBinarySecret)).when(mockSecretsGroup).getLatestActiveVersion(binarySecretIdentifier);
        doReturn(Optional.of(versionedBinarySecret)).when(mockSecretsGroup).getActive(binarySecretIdentifier, version);
        doReturn(Arrays.asList(secretStringEntry2, secretStringEntry3, secretBinaryEntry2, secretBinaryEntry3)).when(mockSecretsGroup).getLatestActiveVersionOfAllSecrets();

        simpleSecretsGroup = new DefaultSimpleSecretsGroup(mockSecretsGroup);
    }

    @Test
    public void getAllStringSecrets() {
        List<StringSecretEntry> secrets = simpleSecretsGroup.getAllStringSecrets();
        StringSecretEntry one = new StringSecretEntry(stringSecretIdentifier2, 1l, stringSecretValue2.asString());
        StringSecretEntry two = new StringSecretEntry(stringSecretIdentifier3, 1l, stringSecretValue3.asString());
        assertThat(secrets, is(Arrays.asList(one, two)));
    }

    @Test
    public void getAllByteSecrets() {
        List<ByteSecretEntry> secrets = simpleSecretsGroup.getAllBinarySecrets();
        ByteSecretEntry one = new ByteSecretEntry(binarySecretIdentifier2, 1l, binarySecretValue2.asByteArray());
        ByteSecretEntry two = new ByteSecretEntry(binarySecretIdentifier3, 1l, binarySecretValue3.asByteArray());
        assertThat(secrets, is(Arrays.asList(one, two)));
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
