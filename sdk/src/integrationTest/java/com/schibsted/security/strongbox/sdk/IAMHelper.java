/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.*;
import com.schibsted.security.strongbox.sdk.types.Region;

/**
 * @author stiankri
 * @author kvlees
 */
public class IAMHelper {
    private final AmazonIdentityManagement client;
    public static final String PATH = "/secretmanagementtest/";

    IAMHelper(AWSCredentialsProvider awsCredentials, Region testRegion) {
        this.client = AmazonIdentityManagementClientBuilder.standard()
                .withCredentials(awsCredentials)
                .withRegion(testRegion.getName())
                .build();
    }

    private static String getPolicyDocument(String testUserArn) {
        return "{\n" +
               " \"Version\": \"2012-10-17\",\n" +
               "  \"Statement\": [\n" +
               "   {\n" +
               "      \"Effect\": \"Allow\",\n" +
               "      \"Principal\": {\n" +
               "        \"AWS\": \"" + testUserArn + "\"\n" +
               "      },\n" +
               "      \"Action\": \"sts:AssumeRole\"\n" +
               "    }\n" +
               "  ]\n" +
               "}";

    }

    public String createOrGetRole(String roleName) {
        String userArn = this.client.getUser().getUser().getArn();
        String policyDocument = getPolicyDocument(userArn);

        try {
            CreateRoleRequest request = new CreateRoleRequest();
            request.withPath(PATH).withRoleName(roleName).withAssumeRolePolicyDocument(policyDocument);

            CreateRoleResult result = client.createRole(request);
            return result.getRole().getArn();
        } catch (EntityAlreadyExistsException e) {
            GetRoleRequest getRoleRequest = new GetRoleRequest();
            getRoleRequest.withRoleName(roleName);
            GetRoleResult getRoleResult = client.getRole(getRoleRequest);
            return getRoleResult.getRole().getArn();
        }
    }

    public void deleteRole(String roleName) {
        DeleteRoleRequest request = new DeleteRoleRequest();
        request.withRoleName(roleName);
        client.deleteRole(request);
    }
}
