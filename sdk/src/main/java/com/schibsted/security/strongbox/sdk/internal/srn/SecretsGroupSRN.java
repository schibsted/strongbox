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

package com.schibsted.security.strongbox.sdk.internal.srn;

import com.google.common.base.Objects;
import com.schibsted.security.strongbox.sdk.types.SRN;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;

/**
 * @author stiankri
 */
public class SecretsGroupSRN extends SRN {
    public final String account;
    public final SecretsGroupIdentifier groupIdentifier;

    private static final String RESOURCE_PREFIX = "group";

    public SecretsGroupSRN(String account, SecretsGroupIdentifier groupIdentifier) {
        this.account = account;
        this.groupIdentifier = groupIdentifier;
    }

    @Override
    public String toSrn() {
        return String.format("srn:aws:strongbox:%s:%s:%s/%s", groupIdentifier.region.getName(), account, RESOURCE_PREFIX, groupIdentifier.name.replace('.','/'));
    }

    @Override
    public String toString() {
        return toSrn();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(account, groupIdentifier);
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof SecretsGroupSRN){
            final SecretsGroupSRN other = (SecretsGroupSRN) obj;
            return Objects.equal(account, other.account)
                    && Objects.equal(groupIdentifier, other.groupIdentifier);
        } else{
            return false;
        }
    }
}
