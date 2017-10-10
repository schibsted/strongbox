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

package com.schibsted.security.strongbox.sdk.internal.config;

import com.amazonaws.auth.profile.internal.ProfileKeyConstants;

/**
 * @author stiankri
 */
public enum AWSConfigPropertyKey {
    MFA_SERIAL("mfa_serial"),
    SOURCE_PROFILE(ProfileKeyConstants.SOURCE_PROFILE),
    ROLE_ARN(ProfileKeyConstants.ROLE_ARN),
    AWS_ACCESS_KEY_ID(ProfileKeyConstants.AWS_ACCESS_KEY_ID),
    AWS_SECRET_ACCESS_KEY(ProfileKeyConstants.AWS_SECRET_ACCESS_KEY);

    private final String name;

    AWSConfigPropertyKey(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
