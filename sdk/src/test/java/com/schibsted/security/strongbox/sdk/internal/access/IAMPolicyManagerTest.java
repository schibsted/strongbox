/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.access;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AttachGroupPolicyRequest;
import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.AttachUserPolicyRequest;
import com.amazonaws.services.identitymanagement.model.CreatePolicyRequest;
import com.amazonaws.services.identitymanagement.model.CreatePolicyResult;
import com.amazonaws.services.identitymanagement.model.DeletePolicyRequest;
import com.amazonaws.services.identitymanagement.model.DetachRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.DetachUserPolicyRequest;
import com.amazonaws.services.identitymanagement.model.ListEntitiesForPolicyRequest;
import com.amazonaws.services.identitymanagement.model.ListEntitiesForPolicyResult;
import com.amazonaws.services.identitymanagement.model.ListPoliciesRequest;
import com.amazonaws.services.identitymanagement.model.ListPoliciesResult;
import com.amazonaws.services.identitymanagement.model.Policy;
import com.amazonaws.services.identitymanagement.model.PolicyGroup;
import com.amazonaws.services.identitymanagement.model.PolicyRole;
import com.amazonaws.services.identitymanagement.model.PolicyUser;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.DescribeKeyRequest;
import com.amazonaws.services.kms.model.DescribeKeyResult;
import com.amazonaws.services.kms.model.KeyMetadata;
import com.schibsted.security.strongbox.sdk.internal.encryption.KMSEncryptor;
import com.schibsted.security.strongbox.sdk.internal.encryption.KMSManager;
import com.schibsted.security.strongbox.sdk.internal.RegionLocalResourceName;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generated.DynamoDB;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.schibsted.security.strongbox.sdk.types.EncryptionStrength;
import com.schibsted.security.strongbox.sdk.types.Principal;
import com.schibsted.security.strongbox.sdk.types.PrincipalType;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.testng.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author kvlees
 * @author stiankri
 */
public class IAMPolicyManagerTest {
    private static final String ACCOUNT="1234";
    private static final String ADMIN_POLICY_ARN="arn:aws:iam::1234:policy/strongbox/strongbox_us-west-1_test-group_admin";
    private static final String READONLY_POLICY_ARN="arn:aws:iam::1234:policy/strongbox/strongbox_us-west-1_test-group_readonly";
    private static final String KMS_ARN = "arn:aws:kms:us-west-1:1234:key/d413a4de-5eb5-4eb4-b4af-373bcba5efdf";
    private static final String KMS_ALIAS_ARN = "arn:aws:kms:us-west-1:1234:alias/strongbox_us-west-1_test-group";
    private static final String DYNAMODB_ARN = "arn:aws:dynamodb:us-west-1:1234:table/strongbox_us-west-1_test-group";
    private static final String TEST_DATA_DIR = "src/test/resources";

    private static SecretsGroupIdentifier group = new SecretsGroupIdentifier(
            Region.US_WEST_1, "test.group");
    private static String tableName = new RegionLocalResourceName(group).toString();

    private AmazonIdentityManagementClient mockClient;
    private AWSCredentialsProvider mockCredentials;
    private AmazonDynamoDBClient mockDynamoDBClient;
    private AWSKMSClient mockKMSClient;

    private IAMPolicyManager partiallyMockedPolicyManager;
    private KMSEncryptor kmsEncryptor;
    private DynamoDB partiallyMockedStore;

    @BeforeMethod
    public void setUp() {
        mockCredentials = mock(AWSCredentialsProvider.class);
        mockClient = mock(AmazonIdentityManagementClient.class);
        ClientConfiguration mockConfig = mock(ClientConfiguration.class);
        IAMPolicyManager policyManager = new IAMPolicyManager(mockClient, mockCredentials, mockConfig);

        // The mockito spy acts like original object but mocks out the getAccount() method. As the getAccount() calls
        // directly rather than via a client that we can pass in we need to mock this out using a spy.
        partiallyMockedPolicyManager = spy(policyManager);
        doReturn(ACCOUNT).when(partiallyMockedPolicyManager).getAccount();

        // Set up KMSEncryptor for testing the policy creation methods. This gets a bit complicated but we need to
        // mock all the AWS dependencies from the KMSManager before using it to create the KMSEncryptor. The getAliasArn
        // needs to be mocked out with a spy to stop the call to getAccount.
        mockKMSClient = mock(AWSKMSClient.class);
        KMSManager kmsManager = new KMSManager(mockKMSClient, mockCredentials, mockConfig, group);
        KMSManager partiallyMockedKMSManager = spy(kmsManager);
        doReturn(KMS_ALIAS_ARN).when(partiallyMockedKMSManager).getAliasArn();
        kmsEncryptor = new KMSEncryptor(partiallyMockedKMSManager, mockCredentials, mockConfig, group, mock(AwsCrypto.class), EncryptionStrength.AES_256);

        // Set up store for testing the policy creation methods. Mock out the getArn method with a spy to stop the
        // call to getAccount().
        mockDynamoDBClient = mock(AmazonDynamoDBClient.class);
        DynamoDB store = new DynamoDB(mockDynamoDBClient, mockCredentials, mockConfig, group, new ReentrantReadWriteLock());
        partiallyMockedStore = spy(store);
        doReturn(DYNAMODB_ARN).when(partiallyMockedStore).getArn();
    }

    @Test
    public void testGetAdminPolicyArn() throws Exception {
        String arn = partiallyMockedPolicyManager.getAdminPolicyArn(group);
        assertEquals(arn, ADMIN_POLICY_ARN);
    }

    @Test
    public void testGetReadOnlyArn() throws Exception {
        String arn = partiallyMockedPolicyManager.getReadOnlyArn(group);
        assertEquals(arn, READONLY_POLICY_ARN);
    }

    @Test
    public void testAttachAdminUser() throws Exception {
        Principal principal = new Principal(PrincipalType.USER, "alice");
        partiallyMockedPolicyManager.attachAdmin(group, principal);

        // Just verifies that the attachUserPolicy method was called with the correct details.
        AttachUserPolicyRequest request = new AttachUserPolicyRequest()
            .withPolicyArn(ADMIN_POLICY_ARN)
            .withUserName(principal.name);
        verify(mockClient, times(1)).attachUserPolicy(request);
    }

    @Test
    public void testAttachAdminGroup() throws Exception {
        Principal principal = new Principal(PrincipalType.GROUP, "awesome-team");
        partiallyMockedPolicyManager.attachAdmin(group, principal);

        // Just verifies that the attachRolePolicy method was called with the correct details.
        AttachGroupPolicyRequest request = new AttachGroupPolicyRequest()
                .withPolicyArn(ADMIN_POLICY_ARN)
                .withGroupName(principal.name);
        verify(mockClient, times(1)).attachGroupPolicy(request);
    }

    @Test
    public void testAttachReadOnly() throws Exception {
        Principal principal = new Principal(PrincipalType.USER, "bob");
        partiallyMockedPolicyManager.attachReadOnly(group, principal);

        // Just verifies that the attachUserPolicy method was called with the correct details.
        AttachUserPolicyRequest request = new AttachUserPolicyRequest()
                .withPolicyArn(READONLY_POLICY_ARN)
                .withUserName(principal.name);
        verify(mockClient, times(1)).attachUserPolicy(request);
    }

    @Test
    public void testAttachReadonlyRole() throws Exception {
        Principal principal = new Principal(PrincipalType.ROLE, "awesome-service");
        partiallyMockedPolicyManager.attachReadOnly(group, principal);

        // Just verifies that the attachRolePolicy method was called with the correct details.
        AttachRolePolicyRequest request = new AttachRolePolicyRequest()
                .withPolicyArn(READONLY_POLICY_ARN)
                .withRoleName(principal.name);
        verify(mockClient, times(1)).attachRolePolicy(request);
    }

    @Test
    public void testDetachAdminUser() throws Exception {
        Principal principal = new Principal(PrincipalType.USER, "alice");
        partiallyMockedPolicyManager.detachAdmin(group, principal);

        // Just verifies that the attachUserPolicy method was called with the correct details.
        DetachUserPolicyRequest request = new DetachUserPolicyRequest()
                .withPolicyArn(ADMIN_POLICY_ARN)
                .withUserName(principal.name);
        verify(mockClient, times(1)).detachUserPolicy(request);
    }

    @Test
    public void testDetachReadonlyRole() throws Exception {
        Principal principal = new Principal(PrincipalType.ROLE, "awesome-service");
        partiallyMockedPolicyManager.detachReadOnly(group, principal);

        // Just verifies that the attachRolePolicy method was called with the correct details.
        DetachRolePolicyRequest request = new DetachRolePolicyRequest()
                .withPolicyArn(READONLY_POLICY_ARN)
                .withRoleName(principal.name);
        verify(mockClient, times(1)).detachRolePolicy(request);
    }

    @Test
    public void testDetachAllPrincipals() throws Exception {
        // test calls correct request
    }

    @Test
    public void testListAttachedAdminNoneAttached() throws Exception {
        ListEntitiesForPolicyRequest request = new ListEntitiesForPolicyRequest()
                .withPolicyArn(ADMIN_POLICY_ARN);
        ListEntitiesForPolicyResult result = new ListEntitiesForPolicyResult();
        when(mockClient.listEntitiesForPolicy(request)).thenReturn(result);

        List<Principal> list = partiallyMockedPolicyManager.listAttachedAdmin(group);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testListAttachedAdmin() throws Exception {
        ListEntitiesForPolicyRequest request = new ListEntitiesForPolicyRequest()
                .withPolicyArn(ADMIN_POLICY_ARN);
        PolicyGroup policyGroup = new PolicyGroup().withGroupName("awesome-group");
        PolicyRole policyRole = new PolicyRole().withRoleName("awesome-service");
        PolicyUser policyUser = new PolicyUser().withUserName("bob");
        ListEntitiesForPolicyResult result = new ListEntitiesForPolicyResult()
                .withPolicyGroups(policyGroup)
                .withPolicyUsers(policyUser)
                .withPolicyRoles(policyRole);
        when(mockClient.listEntitiesForPolicy(request)).thenReturn(result);

        List<Principal> list = partiallyMockedPolicyManager.listAttachedAdmin(group);
        assertEquals(list.size(), 3);
        assertEquals(list.get(0), new Principal(PrincipalType.GROUP, "awesome-group"));
        assertEquals(list.get(1), new Principal(PrincipalType.USER, "bob"));
        assertEquals(list.get(2), new Principal(PrincipalType.ROLE, "awesome-service"));
    }

    @Test
    public void testListAttachedReadOnly() throws Exception {
        ListEntitiesForPolicyRequest request = new ListEntitiesForPolicyRequest()
                .withPolicyArn(READONLY_POLICY_ARN);
        PolicyGroup policyGroup = new PolicyGroup().withGroupName("awesome-group");
        PolicyRole policyRole = new PolicyRole().withRoleName("awesome-service");
        PolicyUser policyUser = new PolicyUser().withUserName("alice");
        ListEntitiesForPolicyResult result = new ListEntitiesForPolicyResult()
                .withPolicyGroups(policyGroup)
                .withPolicyUsers(policyUser)
                .withPolicyRoles(policyRole);
        when(mockClient.listEntitiesForPolicy(request)).thenReturn(result);

        List<Principal> list = partiallyMockedPolicyManager.listAttachedReadOnly(group);
        assertEquals(list.size(), 3);
        assertEquals(list.get(0), new Principal(PrincipalType.GROUP, "awesome-group"));
        assertEquals(list.get(1), new Principal(PrincipalType.USER, "alice"));
        assertEquals(list.get(2), new Principal(PrincipalType.ROLE, "awesome-service"));
    }

    @Test
    public void testGetSecretsGroupIdentifiersNoGroups() throws Exception {
        ListPoliciesRequest request = new ListPoliciesRequest().withMaxItems(1000).withPathPrefix("/strongbox/");
        when(mockClient.listPolicies(request)).thenReturn(new ListPoliciesResult());

        Set<SecretsGroupIdentifier> identifiers = partiallyMockedPolicyManager.getSecretsGroupIdentifiers();
        assertTrue(identifiers.isEmpty());
    }

    @Test
    public void testGetSecretsGroupIdentifiers() throws Exception {
        ListPoliciesRequest request = new ListPoliciesRequest().withMaxItems(1000).withPathPrefix("/strongbox/");
        Policy policyUS1 = new Policy().withPolicyName("strongbox_us-west-1_test-group1_admin");
        Policy policyUS2 = new Policy().withPolicyName("strongbox_us-west-1_test-group2_admin");
        Policy policyEU1 = new Policy().withPolicyName("strongbox_eu-west-1_test-group1_admin");
        Policy policyEU1readonly = new Policy().withPolicyName("strongbox_eu-west-1_test-group1_readonly");
        ListPoliciesResult result = new ListPoliciesResult()
                .withPolicies(policyUS1, policyUS2, policyEU1, policyEU1readonly);
        when(mockClient.listPolicies(request)).thenReturn(new ListPoliciesResult()
                .withPolicies(policyUS1, policyUS2, policyEU1, policyEU1readonly));

        Set<SecretsGroupIdentifier> identifiers = partiallyMockedPolicyManager.getSecretsGroupIdentifiers();
        assertEquals(identifiers.size(), 3);
        assertTrue(identifiers.contains(new SecretsGroupIdentifier(Region.US_WEST_1, "test.group1")));
        assertTrue(identifiers.contains(new SecretsGroupIdentifier(Region.US_WEST_1, "test.group2")));
        assertTrue(identifiers.contains(new SecretsGroupIdentifier(Region.EU_WEST_1, "test.group1")));

        verify(mockClient, times(1)).listPolicies(request);
    }

    private CreatePolicyRequest constructCreatePolicyRequest(String accessType, String policyDocument) {
        return new CreatePolicyRequest()
                .withPolicyName(String.format("strongbox_us-west-1_test-group_%s", accessType))
                .withDescription(String.format("This policy is managed by Strongbox. This policy grants %s permissions.", accessType))
                .withPolicyDocument(policyDocument)
                .withPath("/strongbox/");
    }

    private DescribeKeyResult constructDescribeKeyResult() {
        KeyMetadata keyMetadata = new KeyMetadata().withArn(KMS_ARN);
        return new DescribeKeyResult().withKeyMetadata(keyMetadata);
    }

    @Test
    public void testCreateAdminPolicy() throws Exception {
        String policyDocument = new String(Files.readAllBytes(Paths.get(TEST_DATA_DIR, "test_admin_policy")));
        CreatePolicyRequest request = constructCreatePolicyRequest("admin", policyDocument);
        CreatePolicyResult result = new CreatePolicyResult().withPolicy(new Policy().withArn(ADMIN_POLICY_ARN));
        when(mockClient.createPolicy(request)).thenReturn(result);

        // When constructing policy statement for KMS, the KMSManager checks that the key exists with a
        // DescribeKeyRequest. So we need to mock this result as well.
        DescribeKeyRequest keyRequest = new DescribeKeyRequest().withKeyId(KMS_ALIAS_ARN);
        when(mockKMSClient.describeKey(keyRequest)).thenReturn(constructDescribeKeyResult());

        // Create the policy and verify the policy is as expected and expected calls to AWS were made.
        String policyArn = partiallyMockedPolicyManager.createAdminPolicy(group, kmsEncryptor, partiallyMockedStore);

        verify(mockClient, times(1)).createPolicy(request);
        verify(mockKMSClient, times(1)).describeKey(keyRequest);
        assertEquals(policyArn, ADMIN_POLICY_ARN);
    }

    @Test
    public void testCreateReadOnlyPolicy() throws Exception {
        String policyDocument = new String(Files.readAllBytes(Paths.get(TEST_DATA_DIR, "test_readonly_policy")));
        CreatePolicyRequest request = constructCreatePolicyRequest("readonly", policyDocument);
        CreatePolicyResult result = new CreatePolicyResult().withPolicy(new Policy().withArn(READONLY_POLICY_ARN));
        when(mockClient.createPolicy(request)).thenReturn(result);

        // When constructing policy statement for KMS, the KMSManager checks that the key exists with a
        // DescribeKeyRequest. So we need to mock this result as well.
        DescribeKeyRequest keyRequest = new DescribeKeyRequest().withKeyId(KMS_ALIAS_ARN);
        when(mockKMSClient.describeKey(keyRequest)).thenReturn(constructDescribeKeyResult());

        // Create the policy and verify the policy is as expected and expected calls to AWS were made.
        String policyArn = partiallyMockedPolicyManager.createReadOnlyPolicy(group, kmsEncryptor, partiallyMockedStore);
        verify(mockClient, times(1)).createPolicy(request);
        verify(mockKMSClient, times(1)).describeKey(keyRequest);
        assertEquals(policyArn, READONLY_POLICY_ARN);
    }

    @Test
    public void testDeleteAdminPolicy() throws Exception {
        DeletePolicyRequest request = new DeletePolicyRequest().withPolicyArn(ADMIN_POLICY_ARN);
        partiallyMockedPolicyManager.deleteAdminPolicy(group);
        verify(mockClient, times(1)).deletePolicy(request);
    }

    @Test
    public void testDeleteReadonlyPolicy() throws Exception {
        DeletePolicyRequest request = new DeletePolicyRequest().withPolicyArn(READONLY_POLICY_ARN);
        partiallyMockedPolicyManager.deleteReadonlyPolicy(group);
        verify(mockClient, times(1)).deletePolicy(request);
    }
}
