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

package com.schibsted.security.strongbox.sdk.types;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.schibsted.security.strongbox.sdk.internal.srn.SecretsGroupSRN;

import java.util.List;
import java.util.Optional;

/**
 * @author stiankri
 */
public final class SecretsGroupInfo {
    public SecretsGroupSRN srn;
    public Optional<String> encryptorArn;
    public Optional<String> storageArn;
    public Optional<String> adminPolicyArn;
    public Optional<String> readOnlyPolicyArn;
    public List<Principal> admin;
    public List<Principal> readOnly;

    public SecretsGroupInfo(SecretsGroupSRN srn, Optional<String> encryptorArn, Optional<String> storageArn, Optional<String> adminPolicyArn, Optional<String> readOnlyPolicyArn, List<Principal> admin, List<Principal> readOnly) {
        this.srn = srn;
        this.encryptorArn = encryptorArn;
        this.storageArn = storageArn;
        this.adminPolicyArn = adminPolicyArn;
        this.readOnlyPolicyArn = readOnlyPolicyArn;
        this.admin = admin;
        this.readOnly = readOnly;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("srn", srn)
                .add("storageArn", storageArn)
                .add("encryptorArn", encryptorArn)
                .add("adminPolicyArn", adminPolicyArn)
                .add("readOnlyPolicyArn", readOnlyPolicyArn)
                .add("admin", admin)
                .add("readOnly", readOnly)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(encryptorArn, storageArn, adminPolicyArn, readOnlyPolicyArn, admin, readOnly);
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof SecretsGroupInfo){
            final SecretsGroupInfo other = (SecretsGroupInfo) obj;
            return Objects.equal(srn, other.srn)
                    && Objects.equal(encryptorArn, other.encryptorArn)
                    && Objects.equal(storageArn, other.storageArn)
                    && Objects.equal(adminPolicyArn, other.adminPolicyArn)
                    && Objects.equal(readOnlyPolicyArn, other.readOnlyPolicyArn)
                    && Objects.equal(admin, other.admin)
                    && Objects.equal(readOnly, other.readOnly);
        } else{
            return false;
        }
    }
}
