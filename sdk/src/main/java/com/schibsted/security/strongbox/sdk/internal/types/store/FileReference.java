/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.types.store;

import com.google.common.base.Objects;

import java.io.File;

/**
 * @author stiankri
 */
public class FileReference extends StorageReference {
    public File path;

    public FileReference(File path) {
        this.path = path;
        this.storageType = StorageType.FILE;
    }

    @Override
    public String toString() {
        return String.format("file [%s]", this.path.getAbsolutePath());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(storageType, path);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof FileReference) {
            final FileReference other = (FileReference) obj;
            return Objects.equal(path.getAbsolutePath(), other.path.getAbsolutePath());
        } else {
            return false;
        }
    }
}
