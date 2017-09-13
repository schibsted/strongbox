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

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.schibsted.security.strongbox.sdk.impl.DefaultSecretsGroupManager;
import com.schibsted.security.strongbox.sdk.types.Principal;
import com.schibsted.security.strongbox.sdk.types.PrincipalType;
import com.schibsted.security.strongbox.sdk.types.Region;

/**
 * @author stiankri
 */
public class AssumedRoleTestContext {
    public static final String ROLE_PREFIX = "secret-manager-integration-test";
    public final String roleName;
    public final String roleArn;
    public final Principal principal;
    public final AWSCredentialsProvider assumedAWSCredentials;
    public final SecretsGroupManager secretGroupManager;

    private IAMHelper iamHelper;

    public static AssumedRoleTestContext setup(Region testRegion, String roleSuffix) {
        return new AssumedRoleTestContext(testRegion, roleSuffix);
    }

    private AssumedRoleTestContext(Region testRegion, String roleSuffix) {
        this(new DefaultAWSCredentialsProviderChain(), testRegion, roleSuffix);
    }

    public AssumedRoleTestContext(AWSCredentialsProvider awsCredentials, Region testRegion, String roleSuffix) {
        iamHelper = new IAMHelper(awsCredentials, testRegion);

        roleName = ROLE_PREFIX + roleSuffix;
        roleArn = iamHelper.createOrGetRole(roleName);
        principal = new Principal(PrincipalType.ROLE, roleName);

        STSAssumeRoleSessionCredentialsProvider.Builder builder = new STSAssumeRoleSessionCredentialsProvider.Builder(roleArn, "mysession");
        assumedAWSCredentials = builder.build();
        secretGroupManager = new DefaultSecretsGroupManager(assumedAWSCredentials);
    }

    public void teardown() {
        iamHelper.deleteRole(roleName);
    }

}
