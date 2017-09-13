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
