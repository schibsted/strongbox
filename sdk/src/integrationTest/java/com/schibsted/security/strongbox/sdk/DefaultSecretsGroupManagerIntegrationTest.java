/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.schibsted.security.strongbox.sdk.exceptions.AlreadyExistsException;
import com.schibsted.security.strongbox.sdk.exceptions.DoesNotExistException;
import com.schibsted.security.strongbox.sdk.exceptions.SecretsGroupException;
import com.schibsted.security.strongbox.sdk.impl.DefaultSecretsGroupManager;
import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;
import com.schibsted.security.strongbox.sdk.internal.types.config.UserConfig;
import com.schibsted.security.strongbox.sdk.types.Comment;
import com.schibsted.security.strongbox.sdk.types.EncryptionStrength;
import com.schibsted.security.strongbox.sdk.types.NewSecretEntry;
import com.schibsted.security.strongbox.sdk.types.Principal;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretMetadata;
import com.schibsted.security.strongbox.sdk.types.SecretType;
import com.schibsted.security.strongbox.sdk.types.SecretValue;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupInfo;
import com.schibsted.security.strongbox.sdk.types.State;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.fail;

/**
 * @author kvlees
 * @author stiankri
 */
public class DefaultSecretsGroupManagerIntegrationTest {
    private static final Region TEST_REGION = Region.EU_WEST_1;
    private static final String GROUP_PREFIX = "sm.tests.defaultsecretsgroupmanager.main";

    private static String testRunId = String.valueOf(System.currentTimeMillis());
    private static SecretsGroupIdentifier identifier = new SecretsGroupIdentifier(
            TEST_REGION, GROUP_PREFIX + "t" + testRunId);

    private static SecretsGroupManager secretsGroupManager = new DefaultSecretsGroupManager(
            new DefaultAWSCredentialsProviderChain(), new UserConfig(), EncryptionStrength.AES_128);
    private static AssumedRoleTestContext readOnlyContext;
    private static AssumedRoleTestContext adminContext;
    private static AssumedRoleTestContext otherContext;
    private static SecretsGroupInfo creationInfo;

    private static final SecretIdentifier secretOneIdentifier = new SecretIdentifier("mySecret");
    private static final SecretIdentifier secretTwoIdentifier = new SecretIdentifier("myOtherSecret");

    private static final SecretValue secretOneVersionOneValue = new SecretValue("0123", SecretType.OPAQUE);
    private static final SecretValue secretOneValue = new SecretValue("1234", SecretType.OPAQUE);
    private static final SecretValue secretTwoValue = new SecretValue("#$%^&*()!@", SecretType.OPAQUE);

    private static void setUpSecrets() {
        SecretsGroup secretsGroup = secretsGroupManager.get(identifier);
        NewSecretEntry newSecretEntry1 = new NewSecretEntry(secretOneIdentifier, secretOneVersionOneValue, State.ENABLED);
        NewSecretEntry newSecretEntry2 = new NewSecretEntry(secretOneIdentifier, secretOneValue, State.ENABLED);
        NewSecretEntry newSecretEntry3 = new NewSecretEntry(secretOneIdentifier, new SecretValue("2345", SecretType.OPAQUE), State.DISABLED);
        secretsGroup.create(newSecretEntry1);
        secretsGroup.addVersion(newSecretEntry2);
        secretsGroup.addVersion(newSecretEntry3);

        // Add a second secret.
        NewSecretEntry newSecretEntry4 = new NewSecretEntry(secretTwoIdentifier, secretTwoValue, State.ENABLED);
        secretsGroup.create(newSecretEntry4);

        // Verify both secrets were added.
        Set<SecretIdentifier> secrets = secretsGroup.identifiers();
        assertThat(secrets, hasSize(2));
        assertThat(secrets, containsInAnyOrder(secretOneIdentifier, secretTwoIdentifier));
    }

    private static void tearDownSecrets() {
        SecretsGroup secretsGroup = secretsGroupManager.get(identifier);
        secretsGroup.delete(secretOneIdentifier);
        secretsGroup.delete(secretTwoIdentifier);
        assertThat(secretsGroup.identifiers(), is(empty()));
    }

    private static void attachReadonly(SecretsGroupManager manager, Principal principal) {
        manager.attachReadOnly(identifier, principal);
        SecretsGroupInfo info = manager.info(identifier);
        assertThat(info.readOnly, hasItem(principal));
    }

    private static void attachAdmin(SecretsGroupManager manager, Principal principal) {
        manager.attachAdmin(identifier, principal);
        SecretsGroupInfo info = manager.info(identifier);
        assertThat(info.admin, hasItem(principal));
    }

    private static void detachReadonly(SecretsGroupManager manager, Principal principal) {
        manager.detachReadOnly(identifier, principal);
        SecretsGroupInfo info2 = manager.info(identifier);
        assertThat(info2.readOnly, not(hasItem(principal)));
    }

    private static void detachAdmin(SecretsGroupManager manager, Principal principal) {
        manager.detachAdmin(identifier, principal);
        SecretsGroupInfo info = manager.info(identifier);
        assertThat(info.admin, not(hasItem(principal)));
    }

    private void testGetSecrets(SecretsGroup secretsGroup) {
        // Test listing the secret names.
        Set<SecretIdentifier> secretIdentifiers = secretsGroup.identifiers();
        assertThat(secretIdentifiers, hasSize(2));
        assertThat(secretIdentifiers, containsInAnyOrder(secretOneIdentifier, secretTwoIdentifier));

        // GetActive
        assertThat(secretsGroup.getActive(secretOneIdentifier, 1).get().secretValue.asByteArray(), is(Encoder.asUTF8("0123")));
        assertThat(secretsGroup.getActive(secretOneIdentifier, 2).get().secretValue, is(secretOneValue));

        // GetLatestActive
        assertThat(secretsGroup.getLatestActiveVersion(secretOneIdentifier).get().secretValue, is(secretOneValue));
        assertThat(secretsGroup.getLatestActiveVersion(secretTwoIdentifier).get().secretValue, is(secretTwoValue));

        // GetLatestActive for all secrets.
        List<SecretValue> secrets = secretsGroup.getLatestActiveVersionOfAllSecrets().stream()
                .map(s -> s.secretValue)
                .collect(Collectors.toList());
        assertThat(secrets, containsInAnyOrder(secretOneValue, secretTwoValue));

        // GetAllActive
        secrets = secretsGroup.getAllActiveVersions(secretOneIdentifier).stream().map(
                s -> s.secretValue).collect(Collectors.toList());
        assertThat(secrets, containsInAnyOrder(secretOneValue, secretOneVersionOneValue));

        secrets = secretsGroup.getAllActiveVersions(secretTwoIdentifier).stream().map(
                s -> s.secretValue).collect(Collectors.toList());
        assertThat(secrets, contains(secretTwoValue));

        // Get latest for a secret that doesn't exist.
        assertThat(secretsGroup.getLatestActiveVersion(new SecretIdentifier("wrongName")).
                isPresent(), is(false));

        // Calling getActive on a version that isn't active should return Optional.empty().
        assertThat(secretsGroup.getActive(secretOneIdentifier, 3).isPresent(), is(false));
    }

    private void testManageSecrets(SecretsGroup secretsGroup) {
        // Create a new secret.
        SecretIdentifier anotherIdentifier = new SecretIdentifier("anotherSecret");
        NewSecretEntry anotherSecret = new NewSecretEntry(anotherIdentifier, new SecretValue("1234", SecretType.OPAQUE), State.ENABLED);
        secretsGroup.create(anotherSecret);

        // Add a new version.
        NewSecretEntry anotherSecretV2 = new NewSecretEntry(anotherIdentifier, new SecretValue("foobar", SecretType.OPAQUE), State.ENABLED);
        secretsGroup.addVersion(anotherSecretV2);

        // Update the metadata.
        SecretMetadata metadata = new SecretMetadata(
                anotherIdentifier, 2, Optional.of(State.ENABLED), Optional.empty(),
                Optional.empty(), Optional.of(Optional.of(new Comment("a comment"))));
        secretsGroup.update(metadata);

        // Delete the secret.
        secretsGroup.delete(anotherIdentifier);
    }

    @BeforeClass
    public static void before() throws InterruptedException {
        readOnlyContext = AssumedRoleTestContext.setup(TEST_REGION, "readonly." + testRunId);
        adminContext = AssumedRoleTestContext.setup(TEST_REGION, "admin." + testRunId);
        otherContext = AssumedRoleTestContext.setup(TEST_REGION, "other." + testRunId);
        creationInfo = IntegrationTestHelper.createGroup(secretsGroupManager, identifier);
        setUpSecrets();
    }

    @AfterClass
    public static void after() {
        tearDownSecrets();
        detachAdmin(secretsGroupManager, adminContext.principal);
        detachReadonly(secretsGroupManager, readOnlyContext.principal);
        IntegrationTestHelper.cleanupGroup(secretsGroupManager, identifier);
        readOnlyContext.teardown();
        adminContext.teardown();
        otherContext.teardown();

        // Also cleanup anything left over from a previous test run that was not deleted properly.
        IntegrationTestHelper.cleanUpFromPreviousRuns(Regions.fromName(TEST_REGION.getName()), GROUP_PREFIX);
    }

    @Test(groups = "main-user-context")
    public void testReadAndManageGroups() throws InterruptedException {
        // Test can get.
        secretsGroupManager.get(identifier);

        // Test can get group info.
        SecretsGroupInfo info = secretsGroupManager.info(identifier);
        assertThat(info, is(creationInfo));

        // Test can get group list.
        Set<SecretsGroupIdentifier> groups = secretsGroupManager.identifiers();
        assertThat(groups, hasItem(identifier));

        // Test can attach principals.
        attachReadonly(secretsGroupManager, readOnlyContext.principal);
        attachAdmin(secretsGroupManager, adminContext.principal);

        // Sometimes the admin tests fail, it seems to take a while sometimes for the new IAM policies to used by
        // DynamoDB and KMS. So wait until the contexts can access the table.
        int failedAttempts = 0;
        boolean policiesActive = false;
        while (!policiesActive) {
            try {
                // Need to test both the scan and query permissions as the Query seems to fail sometime if the Scan
                // is allowed.
                adminContext.secretGroupManager.get(identifier).identifiers();
                adminContext.secretGroupManager.get(identifier).getLatestActiveVersion(secretOneIdentifier);
                readOnlyContext.secretGroupManager.get(identifier).identifiers();
                adminContext.secretGroupManager.get(identifier).getLatestActiveVersion(secretOneIdentifier);
                policiesActive = true;
            } catch (Exception e) {
                if (failedAttempts > 20) {
                    fail("Policies not propagated. Not point continuing with tests.");
                }
                failedAttempts++;
                System.out.println("Waiting for policies to propagate to DynamoDB..");
                Thread.sleep(1000);
            }
        }
        // Turns out that the tests above is not enough
        // (the theory being that we might hit different nodes),
        // sleep some more
        Thread.sleep(10000);
    }

    @Test(groups = "main-user-context")
    public void testReadAndManageSecrets() {
        SecretsGroup secretsGroup = secretsGroupManager.get(identifier);
        testGetSecrets(secretsGroup);
        testManageSecrets(secretsGroup);
    }

    @Test(groups = "main-user-context", expectedExceptions = AlreadyExistsException.class)
    public void testCreateAlreadyExisting() {
        SecretsGroup secretsGroup = secretsGroupManager.get(identifier);
        NewSecretEntry newSecretEntry = new NewSecretEntry(secretOneIdentifier, new SecretValue("abcd", SecretType.OPAQUE), State.ENABLED);
        secretsGroup.create(newSecretEntry);
    }

    @Test(groups = "main-user-context", expectedExceptions = DoesNotExistException.class)
    public void testAddVersionToNonExistingSecret() {
        SecretsGroup secretsGroup = secretsGroupManager.get(identifier);
        NewSecretEntry newSecretEntry = new NewSecretEntry(new SecretIdentifier("wrongName"), new SecretValue("abcd", SecretType.OPAQUE), State.ENABLED);
        secretsGroup.addVersion(newSecretEntry);
    }

    @Test(groups = "admin-role-context", dependsOnGroups = "main-user-context")
    public void testAdminRoleCanReadAndManageGroups() {
        // Test can get.
        adminContext.secretGroupManager.get(identifier);

        // Test can get group info. As the previous tests added the admin and readonly roles, we need to add
        // them to the expected result.
        SecretsGroupInfo expectedInfo = creationInfo;
        creationInfo.admin.add(adminContext.principal);
        creationInfo.readOnly.add(readOnlyContext.principal);

        SecretsGroupInfo info = adminContext.secretGroupManager.info(identifier);
        assertThat(adminContext.secretGroupManager.info(identifier), is(expectedInfo));

        // Test can get group list.
        Set<SecretsGroupIdentifier> groups = adminContext.secretGroupManager.identifiers();
        assertThat(groups, hasItem(identifier));

        // Test can attach principals.
        attachReadonly(adminContext.secretGroupManager, otherContext.principal);
        attachAdmin(adminContext.secretGroupManager, otherContext.principal);

        // Test can detach principals.
        detachReadonly(adminContext.secretGroupManager, otherContext.principal);
        detachAdmin(adminContext.secretGroupManager, otherContext.principal);
    }

    @Test(groups = "admin-role-context", dependsOnGroups = "main-user-context")
    public void testAdminRoleCanReadAndManageSecrets() {
        SecretsGroup secretsGroup = adminContext.secretGroupManager.get(identifier);
        testGetSecrets(secretsGroup);
        testManageSecrets(secretsGroup);
    }

    @Test(groups = "readonly-role-context", dependsOnGroups = "admin-role-context")
    public void testReadonlyRoleCanReadSecrets() {
        SecretsGroup secretsGroup = readOnlyContext.secretGroupManager.get(identifier);
        testGetSecrets(secretsGroup);
    }

    @Test(groups = "readonly-role-context", dependsOnGroups = "admin-role-context", expectedExceptions = AmazonServiceException.class)
    public void testReadOnlyCannotCreateSecrets() {
        SecretsGroup secretsGroup = readOnlyContext.secretGroupManager.get(identifier);
        secretsGroup.create(new NewSecretEntry(new SecretIdentifier("anotherSecret"), new SecretValue("1234", SecretType.OPAQUE), State.ENABLED));
    }

    @Test(groups = "readonly-role-context", dependsOnGroups = "admin-role-context", expectedExceptions = AmazonServiceException.class)
    public void testReadOnlyCannotAtSecretVersions() {
        SecretsGroup secretsGroup = readOnlyContext.secretGroupManager.get(identifier);
        secretsGroup.addVersion(new NewSecretEntry(secretOneIdentifier, new SecretValue("foobar", SecretType.OPAQUE), State.ENABLED));
    }

    @Test(groups = "readonly-role-context", dependsOnGroups = "admin-role-context", expectedExceptions = AmazonServiceException.class)
    public void testReadOnlyCannotUpdateSecretMetadata() {
        SecretsGroup secretsGroup = readOnlyContext.secretGroupManager.get(identifier);
        SecretMetadata metadata = new SecretMetadata(
                secretOneIdentifier, 2, Optional.of(State.ENABLED), Optional.empty(),
                Optional.empty(), Optional.of(Optional.of(new Comment("a comment"))));
        secretsGroup.update(metadata);
    }

    @Test(groups = "readonly-role-context", dependsOnGroups = "admin-role-context", expectedExceptions = AmazonServiceException.class)
    public void testReadOnlyCannotDeleteSecrets() {
        SecretsGroup secretsGroup = readOnlyContext.secretGroupManager.get(identifier);
        secretsGroup.delete(secretTwoIdentifier);
    }

    @Test(groups = "readonly-role-context", dependsOnGroups = "admin-role-context", expectedExceptions = AmazonServiceException.class)
    public void testReadOnlyCannotListGroups() {
        readOnlyContext.secretGroupManager.identifiers();
    }

    @Test(groups = "readonly-role-context", dependsOnGroups = "admin-role-context", expectedExceptions = SecretsGroupException.class)
    public void testReadOnlyCannotGetGroupInfo() {
        readOnlyContext.secretGroupManager.info(identifier);
    }

    @Test(groups = "readonly-role-context", dependsOnGroups = "admin-role-context", expectedExceptions = AmazonServiceException.class)
    public void testReadOnlyCannotAttachRole() {
        readOnlyContext.secretGroupManager.attachAdmin(identifier, otherContext.principal);
    }

    @Test(groups = "readonly-role-context", dependsOnGroups = "admin-role-context", expectedExceptions = AmazonServiceException.class)
    public void readOnlyCannotAttachReadOnly() {
        readOnlyContext.secretGroupManager.attachReadOnly(identifier, otherContext.principal);
    }
}
