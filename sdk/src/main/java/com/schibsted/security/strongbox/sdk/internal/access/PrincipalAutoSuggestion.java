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

package com.schibsted.security.strongbox.sdk.internal.access;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.ListRolesRequest;
import com.amazonaws.services.identitymanagement.model.ListRolesResult;
import com.schibsted.security.strongbox.sdk.internal.RegionResolver;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.schibsted.security.strongbox.sdk.types.Principal;
import com.schibsted.security.strongbox.sdk.types.PrincipalType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.schibsted.security.strongbox.sdk.internal.ClientConfigurationHelper.transformAndVerifyOrThrow;

/**
 * @author stiankri
 * @author hawkaa
 */
public class PrincipalAutoSuggestion {
    private final AmazonIdentityManagement client;

    public PrincipalAutoSuggestion(AmazonIdentityManagement client) {
        this.client = client;
    }

    public static PrincipalAutoSuggestion fromCredentials(AWSCredentialsProvider awsCredentials, ClientConfiguration clientConfiguration) {

        AmazonIdentityManagement client = AmazonIdentityManagementClientBuilder.standard()
                .withCredentials(awsCredentials)
                .withClientConfiguration(transformAndVerifyOrThrow(clientConfiguration))
                .withRegion(RegionResolver.getRegion())
                .build();

        return new PrincipalAutoSuggestion(client);
    }

    public List<Principal> autoSuggestion(final String name) {
        if (name.length() >= 3) {
            String lowerCaseName = name.toLowerCase();

            ListRolesRequest listRolesRequest = new ListRolesRequest();
            listRolesRequest.withMaxItems(1000);
            ListRolesResult result = client.listRoles(listRolesRequest);
            List<Principal> tmp = result.getRoles().stream()
                    .filter(p -> p.getRoleName().toLowerCase().contains(lowerCaseName))
                    .map(p -> new Principal(PrincipalType.ROLE, p.getRoleName())).collect(Collectors.toList());

            return tmp.subList(0, Math.min(5, tmp.size()));
        }
        return new ArrayList<>();
    }
}
