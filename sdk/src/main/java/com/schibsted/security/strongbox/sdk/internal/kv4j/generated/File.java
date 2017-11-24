/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.kv4j.generated;

import com.schibsted.security.strongbox.sdk.internal.encryption.EncryptionContext;
import com.schibsted.security.strongbox.sdk.internal.encryption.Encryptor;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.backend.file.GenericFile;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * @author stiankri
 */
public class File extends GenericFile<RawSecretEntry, SecretIdentifier, Long> implements Store, AutoCloseable {
    public File(java.io.File path, Encryptor encryptor, EncryptionContext encryptionContext, ReadWriteLock readWriteLock) {
        super(path, Config.converters, encryptor, encryptionContext, RawSecretEntry.class, readWriteLock);
    }
}
