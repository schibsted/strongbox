/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk;

import com.schibsted.security.strongbox.sdk.types.ByteSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.exceptions.EncodingException;
import com.schibsted.security.strongbox.sdk.types.StringSecretEntry;

import java.util.List;
import java.util.Optional;

/**
 * Simplified version of {@code SecretsGroup} to more concisely retrieve secret values
 *
 * @author stiankri
 * @author hawkaa
 */
public interface SimpleSecretsGroup {
    /**
     * Get the latest secret value as a {@code String}
     *
     * @param secretIdentifier identifier of the secret to retrieve
     * @return the secret value as {@code String}
     * @throws EncodingException if the secret is not a {@code String}
     */
    Optional<String> getStringSecret(SecretIdentifier secretIdentifier);

    /**
     * Get the latest secret value as a {@code String}
     *
     * @param secretIdentifier identifier of the secret to retrieve
     * @return the secret value as {@code String}
     * @throws EncodingException if the secret is not a {@code String}
     */
    Optional<String> getStringSecret(String secretIdentifier);

    /**
     * Get a specific secret value version as a {@code String}
     *
     * @param secretIdentifier identifier of the secret to retrieve
     * @param version version of the secret to retrieve
     * @return the secret value as {@code String}
     * @throws EncodingException if the secret is not a {@code String}
     */
    Optional<String> getStringSecret(SecretIdentifier secretIdentifier, long version);

    /**
     * Get a specific secret value version as a {@code String}
     *
     * @param secretIdentifier identifier of the secret to retrieve
     * @param version version of the secret to retrieve
     * @return the secret value as {@code String}
     * @throws EncodingException if the secret is not a {@code String}
     */
    Optional<String> getStringSecret(String secretIdentifier, long version);

    /**
     * Get all latest active versions of String secrets
     *
     * @return List with {@code StringSecretEntry}
     */
    List<StringSecretEntry> getAllStringSecrets();

    /**
     * Get the latest secret value as a {@code byte[]}
     *
     * @param secretIdentifier identifier of the secret to retrieve
     * @return the secret value as {@code byte[]}
     * @throws EncodingException if the secret is not binary
     */
    Optional<byte[]> getBinarySecret(SecretIdentifier secretIdentifier);

    /**
     * Get the latest secret value as a {@code byte[]}
     *
     * @param secretIdentifier identifier of the secret to retrieve
     * @return the secret value as {@code byte[]}
     * @throws EncodingException if the secret is not binary
     */
    Optional<byte[]> getBinarySecret(String secretIdentifier);

    /**
     * Get a specific secret value version as a {@code byte[]}
     *
     * @param secretIdentifier identifier of the secret to retrieve
     * @param version version of the secret to retrieve
     * @return the secret value as {@code byte[]}
     * @throws EncodingException if the secret is not binary
     */
    Optional<byte[]> getBinarySecret(SecretIdentifier secretIdentifier, long version);

    /**
     * Get a specific secret value version as a {@code byte[]}
     *
     * @param secretIdentifier identifier of the secret to retrieve
     * @param version version of the secret to retrieve
     * @return the secret value as {@code byte[]}
     * @throws EncodingException if the secret is not binary
     */
    Optional<byte[]> getBinarySecret(String secretIdentifier, long version);

    /**
     * Get all latest active versions of byte secrets
     *
     * @return List with {@code ByteSecretEntry}
     */
    List<ByteSecretEntry> getAllBinarySecrets();
}
