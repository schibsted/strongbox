/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.impl;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.schibsted.security.strongbox.sdk.SecretsGroup;
import com.schibsted.security.strongbox.sdk.SimpleSecretsGroup;
import com.schibsted.security.strongbox.sdk.exceptions.EncodingException;
import com.schibsted.security.strongbox.sdk.internal.SessionName;
import com.schibsted.security.strongbox.sdk.types.ByteSecretEntry;
import com.schibsted.security.strongbox.sdk.types.Encoding;
import com.schibsted.security.strongbox.sdk.types.SecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import com.schibsted.security.strongbox.sdk.types.StringSecretEntry;
import com.schibsted.security.strongbox.sdk.types.arn.RoleARN;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author stiankri
 * @author hawkaa
 */
public class DefaultSimpleSecretsGroup implements SimpleSecretsGroup {
    private final SecretsGroup secretsGroup;

    DefaultSimpleSecretsGroup(final SecretsGroup secretsGroup) {
        this.secretsGroup = secretsGroup;
    }

    public DefaultSimpleSecretsGroup(final SecretsGroupIdentifier groupIdentifier) {
        this(groupIdentifier, new DefaultAWSCredentialsProviderChain());
    }

    public DefaultSimpleSecretsGroup(final SecretsGroupIdentifier groupIdentifier, final RoleARN role) {
        AWSCredentialsProvider assumedAWSCredentials = new STSAssumeRoleSessionCredentialsProvider.Builder(role.toArn(), SessionName.getSessionName("StrongboxSDK")).build();
        DefaultSecretsGroupManager secretsGroupManager = new DefaultSecretsGroupManager(assumedAWSCredentials);
        secretsGroup = secretsGroupManager.get(groupIdentifier);
    }

    public DefaultSimpleSecretsGroup(final SecretsGroupIdentifier groupIdentifier, final AWSCredentialsProvider credentialsProvider) {
        DefaultSecretsGroupManager secretsGroupManager = new DefaultSecretsGroupManager(credentialsProvider);
        secretsGroup = secretsGroupManager.get(groupIdentifier);
    }

    @Override
    public Optional<String> getStringSecret(final SecretIdentifier secretIdentifier) {
        return asString(secretsGroup.getLatestActiveVersion(secretIdentifier));
    }

    @Override
    public Optional<String> getStringSecret(String secretIdentifier) {
        return getStringSecret(new SecretIdentifier(secretIdentifier));
    }

    @Override
    public Optional<String> getStringSecret(final SecretIdentifier secretIdentifier, long version) {
        return asString(secretsGroup.getActive(secretIdentifier, version));
    }

    @Override
    public Optional<String> getStringSecret(String secretIdentifier, long version) {
        return getStringSecret(new SecretIdentifier(secretIdentifier), version);
    }

    @Override
    public List<StringSecretEntry> getAllStringSecrets() {
        return secretsGroup.getLatestActiveVersionOfAllSecrets()
                .stream()
                .filter(secretEntry -> secretEntry.secretValue.encoding == Encoding.UTF8)
                .map(StringSecretEntry::of)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<byte[]> getBinarySecret(final SecretIdentifier secretIdentifier) {
        return asBinary(secretsGroup.getLatestActiveVersion(secretIdentifier));
    }

    @Override
    public Optional<byte[]> getBinarySecret(String secretIdentifier) {
        return getBinarySecret(new SecretIdentifier(secretIdentifier));
    }

    @Override
    public Optional<byte[]> getBinarySecret(final SecretIdentifier secretIdentifier, final long version) {
        return asBinary(secretsGroup.getActive(secretIdentifier, version));
    }

    @Override
    public Optional<byte[]> getBinarySecret(String secretIdentifier, long version) {
        return getBinarySecret(new SecretIdentifier(secretIdentifier), version);
    }

    @Override
    public List<ByteSecretEntry> getAllBinarySecrets() {
        return secretsGroup.getLatestActiveVersionOfAllSecrets()
                .stream()
                .filter(secretEntry -> secretEntry.secretValue.encoding == Encoding.BINARY)
                .map(ByteSecretEntry::of)
                .collect(Collectors.toList());
    }

    private Optional<String> asString(final Optional<SecretEntry> secretEntry) {
        verifyEncodingOrThrow(secretEntry, Encoding.UTF8);
        return secretEntry.map(e -> e.secretValue.asString());
    }

    private Optional<byte[]> asBinary(final Optional<SecretEntry> secretEntry) {
        verifyEncodingOrThrow(secretEntry, Encoding.BINARY);
        return secretEntry.map(e -> e.secretValue.asByteArray());
    }

    private void verifyEncodingOrThrow(Optional<SecretEntry> secretEntry, Encoding expectedEncoding) {
        if (secretEntry.isPresent() && secretEntry.get().secretValue.encoding != expectedEncoding) {
            throw new EncodingException(String.format("Expected secret value encoding to be '%s' but was '%s'", expectedEncoding, secretEntry.get().secretValue.encoding));
        }
    }
}
