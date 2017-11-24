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

    byte[] encrypt(byte[] data, EncryptionContext context);
    byte[] decrypt(byte[] data, EncryptionContext context);
}
