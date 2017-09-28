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
    List<StringSecretEntry> getAllLatestActiveStringSecrets();

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
    List<ByteSecretEntry> getAllLatestActiveByteSecrets();
}
