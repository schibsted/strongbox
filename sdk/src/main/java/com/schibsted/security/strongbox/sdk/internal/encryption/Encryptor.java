/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

/**
 * Used to encrypt and decrypt data used by Strongbox
 *
 * @author stiankri
 */
public interface Encryptor {
    String encrypt(String data, EncryptionContext context);
    String decrypt(String data, EncryptionContext context);

    /**
     * Encrypt the {@code data} using the {@code context}.
     *
     * The {@code data} is left unchanged and the returned array is allocated
     * by the method. This allows the caller of this method to safely use or
     * clear the {@code data} afterwards.
     *
     * @param data
     *        Plaintext to be encrypted
     * @param context
     *        Encryption Context
     * @return encrypted {@code data}
     */
    byte[] encrypt(byte[] data, EncryptionContext context);

    /**
     * Decrypt the {@code data} using the {@code context}.
     *
     * The {@code data} is left unchanged and the returned array is allocated
     * by the method. This allows the caller of this method to safely use or
     * clear the {@code data} afterwards.
     *
     * @param data
     *        Ciphertext to be decrypted
     * @param context
     *        Encryption Context
     * @return decrypted {@code data}
     */
    byte[] decrypt(byte[] data, EncryptionContext context);
}
