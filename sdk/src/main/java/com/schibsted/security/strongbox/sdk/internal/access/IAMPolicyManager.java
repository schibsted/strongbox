/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.access;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.*;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import com.schibsted.security.strongbox.sdk.exceptions.DoesNotExistException;
import com.schibsted.security.strongbox.sdk.exceptions.UnsupportedTypeException;
import com.schibsted.security.strongbox.sdk.internal.RegionResolver;
import com.schibsted.security.strongbox.sdk.internal.encryption.KMSEncryptor;
import com.schibsted.security.strongbox.sdk.internal.IAMPolicyName;
import com.schibsted.security.strongbox.sdk.internal.AWSResourceNameSerialization;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Store;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.schibsted.security.strongbox.sdk.types.Principal;
import com.schibsted.security.strongbox.sdk.types.PrincipalType;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;

import java.util.*;
import java.util.stream.Collectors;

import static com.schibsted.security.strongbox.sdk.internal.ClientConfigurationHelper.transformAndVerifyOrThrow;

/**
 * @author stiankri
 * @author kvlees
 * @author torarvid
 */
public class IAMPolicyManager {
    public static final String PATH_PREFIX = "/" + AWSResourceNameSerialization.GLOBAL_PREFIX + "/";

    private final AmazonIdentityManagement client;
    private final AWSCredentialsProvider awsCredentials;
    private final ClientConfiguration clientConfiguration;

    private Optional<String> account = Optional.empty();

    public IAMPolicyManager(AmazonIdentityManagement client, AWSCredentialsProvider awsCredentials, ClientConfiguration clientConfiguration) {
        this.awsCredentials = awsCredentials;
        this.client = client;
        this.clientConfiguration = clientConfiguration;
    }

    public static IAMPolicyManager fromCredentials(AWSCredentialsProvider awsCredentials, ClientConfiguration clientConfiguration) {
        AmazonIdentityManagement client = AmazonIdentityManagementClientBuilder.standard()
            .withCredentials(awsCredentials)
            .withClientConfiguration(transformAndVerifyOrThrow(clientConfiguration))
            .withRegion(RegionResolver.getRegion())
            .build();
        return new IAMPolicyManager(client, awsCredentials, clientConfiguration);
    }

    public static String getAccount(AWSCredentialsProvider awsCredentialsProvider, ClientConfiguration clientConfiguration) {
        AWSSecurityTokenService client = AWSSecurityTokenServiceClientBuilder.standard()
            .withCredentials(awsCredentialsProvider)
            .withClientConfiguration(transformAndVerifyOrThrow(clientConfiguration))
            .withRegion(RegionResolver.getRegion())
            .build();
        GetCallerIdentityRequest request = new GetCallerIdentityRequest();
        GetCallerIdentityResult result = client.getCallerIdentity(request);

        return result.getAccount();
    }

    public String getAccount() {
        if (!account.isPresent()) {
            account = Optional.of(getAccount(awsCredentials, clientConfiguration));
        }
        return account.get();
    }

    public boolean adminPolicyExists(SecretsGroupIdentifier group) {
        return policyExists(getAdminPolicyArn(group));
    }

    public boolean readOnlyPolicyExists(SecretsGroupIdentifier group) {
        return policyExists(getReadOnlyArn(group));
    }

    private boolean policyExists(String arn) {
        try {
            GetPolicyRequest request = new GetPolicyRequest();
            request.withPolicyArn(arn);
            client.getPolicy(request);
            return true;
        } catch (NoSuchEntityException e) {
            return false;
        }
    }

    public String getAdminPolicyArn(SecretsGroupIdentifier group) {
        return getArn(group, AccessLevel.ADMIN);
    }

    public String getReadOnlyArn(SecretsGroupIdentifier group) {
        return getArn(group, AccessLevel.READONLY);
    }

    public void attachAdmin(SecretsGroupIdentifier group, Principal principal) {
        attachPrincipalToPolicy(group, principal, AccessLevel.ADMIN);
    }

    public void attachReadOnly(SecretsGroupIdentifier group, Principal principal) {
        attachPrincipalToPolicy(group, principal, AccessLevel.READONLY);
    }

    public void detachAllPrincipals(SecretsGroupIdentifier group) {
        try {
            List<Principal> admins = listAttachedAdmin(group);
            admins.forEach(p -> detachAdmin(group, p));
        } catch (DoesNotExistException e) {
            // policy does not exist: ignore for now
            // TODO: improve robustness if detach throws
        }

        try {
            List<Principal> readonly = listAttachedReadOnly(group);
            readonly.forEach(p -> detachReadOnly(group, p));
        } catch (DoesNotExistException e) {
            // policy does not exist: ignore for now
            // TODO: improve robustness if detach throws
        }

        // TODO: add sanity check at end
    }

    // TODO expose underlying method instead? i.e. the AccessLevel type.
    public void detachAdmin(SecretsGroupIdentifier group, Principal principal) {
        detachPrincipal(group, principal, AccessLevel.ADMIN);
    }

    public void detachReadOnly(SecretsGroupIdentifier group, Principal principal) {
        detachPrincipal(group,principal, AccessLevel.READONLY);
    }

    private void detachPrincipal(SecretsGroupIdentifier group, Principal principal, AccessLevel accessLevel) {
        String policyArn = getArn(group, accessLevel);

        switch (principal.type) {
            case ROLE:
                DetachRolePolicyRequest roleRequest = new DetachRolePolicyRequest();
                roleRequest.withPolicyArn(policyArn).withRoleName(principal.name);
                client.detachRolePolicy(roleRequest);
                break;
            case USER:
                DetachUserPolicyRequest userRequest = new DetachUserPolicyRequest();
                userRequest.withPolicyArn(policyArn).withUserName(principal.name);
                client.detachUserPolicy(userRequest);
                break;
            case GROUP:
                DetachGroupPolicyRequest groupRequest = new DetachGroupPolicyRequest();
                groupRequest.withPolicyArn(policyArn).withGroupName(principal.name);
                client.detachGroupPolicy(groupRequest);
                break;
            default:
                throw new UnsupportedTypeException(principal.type.toString());
        }
    }

    public void attachPrincipalToPolicy(SecretsGroupIdentifier group, Principal principal, AccessLevel accessLevel) {
        String policyArn = getArn(group, accessLevel);

        switch (principal.type) {
            case ROLE:
                AttachRolePolicyRequest roleRequest = new AttachRolePolicyRequest();
                roleRequest.withPolicyArn(policyArn).withRoleName(principal.name);
                client.attachRolePolicy(roleRequest);
                break;
            case USER:
                AttachUserPolicyRequest userRequest = new AttachUserPolicyRequest();
                userRequest.withPolicyArn(policyArn).withUserName(principal.name);
                client.attachUserPolicy(userRequest);
                break;
            case GROUP:
                AttachGroupPolicyRequest groupRequest = new AttachGroupPolicyRequest();
                groupRequest.withPolicyArn(policyArn).withGroupName(principal.name);
                client.attachGroupPolicy(groupRequest);
                break;
            default:
                throw new UnsupportedTypeException(principal.type.toString());
        }
    }

    public List<Principal> listAttachedAdmin(SecretsGroupIdentifier group) {
        return listEntities(group, AccessLevel.ADMIN);
    }

    public List<Principal> listAttachedReadOnly(SecretsGroupIdentifier group) {
        return listEntities(group, AccessLevel.READONLY);
    }

    private List<Principal> listEntities(SecretsGroupIdentifier group, AccessLevel accessLevel) {
        String arn = getArn(group, accessLevel);
        try {
            ListEntitiesForPolicyRequest request = new ListEntitiesForPolicyRequest();
            request.withPolicyArn(arn);

            ListEntitiesForPolicyResult result = client.listEntitiesForPolicy(request);

            List<Principal> all = new ArrayList<>();

            List<Principal> groups = result.getPolicyGroups().stream().map(g -> new Principal(PrincipalType.GROUP, g.getGroupName())).collect(Collectors.toList());
            List<Principal> users = result.getPolicyUsers().stream().map(u -> new Principal(PrincipalType.USER, u.getUserName())).collect(Collectors.toList());
            List<Principal> roles = result.getPolicyRoles().stream().map(r -> new Principal(PrincipalType.ROLE, r.getRoleName())).collect(Collectors.toList());

            all.addAll(groups);
            all.addAll(users);
            all.addAll(roles);

            return all;
        } catch (NoSuchEntityException e) {
            throw new DoesNotExistException(String.format("Could not find policy with ARN: '%s'", arn), e);
        }
    }

    public Set<SecretsGroupIdentifier> getSecretsGroupIdentifiers() {
        ListPoliciesRequest request = new ListPoliciesRequest();
        request.setMaxItems(1000);
        request.setPathPrefix(PATH_PREFIX);
        ListPoliciesResult result = client.listPolicies(request);

        return result.getPolicies().stream()
                .map(p -> IAMPolicyName.fromString(p.getPolicyName()).group).distinct().collect(Collectors.toSet());

    }

    private String getArn(SecretsGroupIdentifier group, AccessLevel accessLevel) {
        IAMPolicyName name = new IAMPolicyName(group, accessLevel);
        return String.format("arn:aws:iam::%s:policy%s%s", getAccount(), PATH_PREFIX, name.toString());
    }

    private String storeReadOnlyPolicyString(Store store) {
        Optional<String> policy = store.awsReadOnlyPolicy();
        return policy.isPresent() ? policy.get() : "";
    }

    private String storeAdminPolicyString(Store store) {
        Optional<String> policy = store.awsAdminPolicy();
        return policy.isPresent() ? policy.get() : "";
    }

    public String createAdminPolicy(final SecretsGroupIdentifier group, final KMSEncryptor kmsEncryptor, final Store store) {
        // TODO: consider using Jackson or something to generate the JSON if AWS does not have anything
        String adminPolicy = "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                kmsEncryptor.awsAdminPolicy().get() + ",\n" + storeAdminPolicyString(store) + ",\n" +
                listAllPolicies() + ",\n" + getPolicyInfo(group) + ",\n" + managePolicies(group) +
                "\n  ]\n" +
                "}";
        return createPolicy(group, AccessLevel.ADMIN, adminPolicy);
    }

    public String createReadOnlyPolicy(SecretsGroupIdentifier group,  KMSEncryptor kmsEncryptor, Store store) {
        String readOnlyPolicy = "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                storeReadOnlyPolicyString(store) + ",\n" + kmsEncryptor.awsReadOnlyPolicy().get() +
                "\n  ]\n" +
                "}";
        return createPolicy(group, AccessLevel.READONLY, readOnlyPolicy);
    }

    private String listAllPolicies() {
        return  "    {\n" +
                "        \"Sid\": \"IAMListAllPolicies\",\n" +
                "        \"Effect\": \"Allow\",\n" +
                "        \"Action\": [\n" +
                "            \"iam:ListPolicies\"\n" +
                "        ],\n" +
                "        \"Resource\": \"" + "arn:aws:iam::" + getAccount() + ":policy" + PATH_PREFIX + "\"\n" +
                "    }";
    }

    private String getPolicyInfo(SecretsGroupIdentifier group) {
        // These are the permissions that can be restricted to the two policies associated with the secret group.
        // The GetPolicy permission is required by the policyExists() method.
        return  "    {\n" +
                "        \"Sid\": \"IAMSecretGroupPolicies\",\n" +
                "        \"Effect\": \"Allow\",\n" +
                "        \"Action\": [\n" +
                "            \"iam:ListEntitiesForPolicy\",\n" +
                "            \"iam:GetPolicy\"\n" +
                "        ],\n" +
                "        \"Resource\": [\n" +
                "            \"" + getAdminPolicyArn(group) + "\",\n" +
                "            \"" + getReadOnlyArn(group) + "\"\n" +
                "        ]\n" +
                "    }";
    }

    private String managePolicies(SecretsGroupIdentifier group) {
        // These are the permissions required for managing the attached policies. The resource needs to be '*' as
        // it seems to refer to the resource being attached rather than the policy being attached to.
        return  "    {\n" +
                "        \"Sid\": \"IAMManagePolicies\",\n" +
                "        \"Effect\": \"Allow\",\n" +
                "        \"Action\": [\n" +
                "            \"iam:AttachRolePolicy\",\n" +
                "            \"iam:AttachGroupPolicy\",\n" +
                "            \"iam:AttachUserPolicy\",\n" +
                "            \"iam:DetachRolePolicy\",\n" +
                "            \"iam:DetachGroupPolicy\",\n" +
                "            \"iam:DetachUserPolicy\"\n" +
                "        ],\n" +
                "        \"Resource\": \"*\",\n" +
                "        \"Condition\": {\n" +
                "            \"ArnEquals\": {\n" +
                "                \"iam:PolicyArn\": [\n" +
                "                    \"" + getAdminPolicyArn(group) + "\",\n" +
                "                    \"" + getReadOnlyArn(group) + "\"\n" +
                "                ]\n" +
                "            }\n" +
                "        }\n" +
                "    }";
    }

    private String createPolicy(SecretsGroupIdentifier group, AccessLevel accessLevel, String policy) {
        IAMPolicyName name = new IAMPolicyName(group, accessLevel);
        String description = "This policy is managed by Strongbox. This policy grants " + accessLevel.toString() + " permissions.";

        CreatePolicyRequest request = new CreatePolicyRequest();

        request.withPolicyName(name.toString()).withDescription(description).withPolicyDocument(policy).withPath(PATH_PREFIX);
        CreatePolicyResult result = client.createPolicy(request);
        return result.getPolicy().getArn();
    }


    public void deleteAdminPolicy(SecretsGroupIdentifier group) {
        deletePolicy(group, AccessLevel.ADMIN);
    }

    public void deleteReadonlyPolicy(SecretsGroupIdentifier group) {
        deletePolicy(group, AccessLevel.READONLY);
    }

    private void deletePolicy(SecretsGroupIdentifier group, AccessLevel accessLevel) {
        String arn = getArn(group, accessLevel);

        DeletePolicyRequest request = new DeletePolicyRequest();
        request.withPolicyArn(arn);

        client.deletePolicy(request);
    }
}
