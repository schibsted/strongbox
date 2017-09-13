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
