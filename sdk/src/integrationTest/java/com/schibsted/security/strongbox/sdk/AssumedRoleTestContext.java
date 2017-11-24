/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
