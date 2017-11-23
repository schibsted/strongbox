/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.DeletePolicyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleRequest;
import com.amazonaws.services.identitymanagement.model.ListPoliciesRequest;
import com.amazonaws.services.identitymanagement.model.ListRolesRequest;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.Policy;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.AliasListEntry;
import com.amazonaws.services.kms.model.DescribeKeyRequest;
import com.amazonaws.services.kms.model.KeyMetadata;
import com.amazonaws.services.kms.model.ScheduleKeyDeletionRequest;
import com.schibsted.security.strongbox.sdk.internal.AWSResourceNameSerialization;
import com.schibsted.security.strongbox.sdk.internal.IAMPolicyName;
import com.schibsted.security.strongbox.sdk.internal.access.IAMPolicyManager;
import com.schibsted.security.strongbox.sdk.internal.encryption.KMSKeyState;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author kvlees
 * @author stiankri
 */
public class IntegrationTestHelper {
    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestHelper.class);


    public static boolean groupExists(SecretsGroupManager secretsGroupManager, SecretsGroupIdentifier identifier) {
        try {
            secretsGroupManager.info(identifier);
            return true;
        } catch (NoSuchElementException | ResourceNotFoundException | NoSuchEntityException e) {
            return false;
        }
    }

    public static void cleanupGroup(SecretsGroupManager secretsGroupManager, SecretsGroupIdentifier identifier) {
        LOG.info("Cleaning up group...");
        if (!groupExists(secretsGroupManager, identifier)) {
            LOG.info("Group does not exist. Nothing to clean up.");
            return;
        }
        secretsGroupManager.delete(identifier);
    }

    public static SecretsGroupInfo createGroup(SecretsGroupManager secretsGroupManager, SecretsGroupIdentifier identifier) {
        LOG.info("Creating a secrets group...");
        return secretsGroupManager.create(identifier);
    }

    public static void cleanUpFromPreviousRuns(Regions testRegion, String groupPrefix) {
        LOG.info("Cleaning up from previous test runs...");

        // Get time an hour ago to clean up anything that was created more than an hour ago. That should be more than
        // enough time for test runs so anything left over by that time will be junk to clean up.
        Date createdBeforeThreshold = new Date(System.currentTimeMillis() - (60 * 60 * 1000));

        // Resource prefix for the test groups so we only clean up the resources related to the tests.
        // TODO is there a method somewhere that will construct this for me so it will always match the
        // actual names constructed by the code?
        String testResourcePrefix = String.format(
                "strongbox_%s_%s", testRegion.getName(),
                AWSResourceNameSerialization.encodeSecretsGroupName(groupPrefix));

        AWSCredentialsProvider awsCredentials = new DefaultAWSCredentialsProviderChain();

        cleanUpDynamoDBTables(testRegion, testResourcePrefix, createdBeforeThreshold, awsCredentials);
        cleanUpKMSKeys(testRegion, testResourcePrefix, createdBeforeThreshold, awsCredentials);
        cleanUpIAM(testRegion, testResourcePrefix, createdBeforeThreshold, awsCredentials);
    }

    private static void cleanUpDynamoDBTables(Regions testRegion, String testResourcePrefix, Date createdBeforeThreshold,
                                              AWSCredentialsProvider awsCredentials) {
        LOG.info("Cleaning DynamoDB...");
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(awsCredentials)
                .withRegion(testRegion)
                .build();

        List<String> tableNames = dynamoDBClient.listTables().getTableNames();
        for (String tableName: tableNames) {
            if (!tableName.startsWith(testResourcePrefix)) {
                continue;
            }
            LOG.info(String.format("Checking if table %s needs cleaning...", tableName));

            try {
                TableDescription desc = dynamoDBClient.describeTable(tableName).getTable();
                if (!desc.getTableName().equals(TableStatus.DELETING.toString()) &&
                        desc.getCreationDateTime() != null &&
                        desc.getCreationDateTime().before(createdBeforeThreshold)) {
                    LOG.info("Cleaning up table: " + tableName);
                    dynamoDBClient.deleteTable(tableName);
                }
            } catch (ResourceNotFoundException e) {
                LOG.info("Looks like table was already cleaned up: " + tableName);
            }
        }
    }

    private static void cleanUpKMSKeys(Regions testRegion, String testResourcePrefix, Date createdBeforeThreshold,
                                       AWSCredentialsProvider awsCredentials) {
        LOG.info("Cleaning KMS...");

        AWSKMS kmsClient = AWSKMSClientBuilder.standard()
                .withCredentials(awsCredentials)
                .withRegion(testRegion)
                .build();

        List<AliasListEntry> keys = kmsClient.listAliases().getAliases();
        for (AliasListEntry entry: keys) {
            if (!entry.getAliasName().startsWith("alias/" + testResourcePrefix)) {
                continue;
            }

            DescribeKeyRequest request = new DescribeKeyRequest().withKeyId(entry.getTargetKeyId());
            KeyMetadata metadata = kmsClient.describeKey(request).getKeyMetadata();

            if (KMSKeyState.fromString(metadata.getKeyState()) != KMSKeyState.PENDING_DELETION &&
                    metadata.getCreationDate().before(createdBeforeThreshold)) {
                LOG.info("Scheduling KMS key for deletion:" + entry.getAliasName());
                scheduleKeyDeletion(kmsClient, entry);
            }
        }
    }

    private static void scheduleKeyDeletion(AWSKMS client, AliasListEntry entry) {
        ScheduleKeyDeletionRequest deletionRequest = new ScheduleKeyDeletionRequest();
        deletionRequest.withKeyId(entry.getTargetKeyId()).withPendingWindowInDays(7);
        client.scheduleKeyDeletion(deletionRequest);
    }

    private static void cleanUpIAM(Regions testRegion, String testResourcePrefix, Date createdBeforeThreshold,
                                   AWSCredentialsProvider awsCredentials) {
        AmazonIdentityManagement iamClient = AmazonIdentityManagementClientBuilder.standard()
            .withCredentials(awsCredentials)
            .withRegion(testRegion)
            .build();
        IAMPolicyManager iamPolicyManager = IAMPolicyManager.fromCredentials(awsCredentials, new ClientConfiguration());

        LOG.info("Cleaning IAM policies...");
        ListPoliciesRequest listPoliciesRequest = new ListPoliciesRequest().withPathPrefix(IAMPolicyManager.PATH_PREFIX);
        List<Policy> policies = iamClient.listPolicies(listPoliciesRequest).getPolicies();
        for (Policy policy: policies) {
            if (policy.getPolicyName().startsWith(testResourcePrefix) &&
                    policy.getCreateDate().before(createdBeforeThreshold)) {
                LOG.info("Cleaning up policy: " + policy.getPolicyName());

                IAMPolicyName iamPolicyName = IAMPolicyName.fromString(policy.getPolicyName());
                iamPolicyManager.detachAllPrincipals(iamPolicyName.group);

                DeletePolicyRequest deletePolicyRequest = new DeletePolicyRequest().withPolicyArn(policy.getArn());
                iamClient.deletePolicy(deletePolicyRequest);
            }
        }

        LOG.info("Cleaning IAM roles created for the assume role tests...");
        ListRolesRequest listRolesRequest = new ListRolesRequest().withPathPrefix(IAMHelper.PATH);
        List<Role> roles = iamClient.listRoles(listRolesRequest).getRoles();
        for (Role role: roles) {
            if (role.getRoleName().startsWith(AssumedRoleTestContext.ROLE_PREFIX) &&
                    role.getCreateDate().before(createdBeforeThreshold)) {
                LOG.info("Cleaning up role: " + role.getRoleName());
                DeleteRoleRequest deleteRoleRequest = new DeleteRoleRequest().withRoleName(role.getRoleName());
                iamClient.deleteRole(deleteRoleRequest);
            }
        }

    }
}
