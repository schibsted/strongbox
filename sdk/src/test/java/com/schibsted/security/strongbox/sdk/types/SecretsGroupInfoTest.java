/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import com.schibsted.security.strongbox.sdk.internal.srn.SecretsGroupSRN;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.*;

/**
 * @author kvlees
 * @author hawkaa
 */
public class SecretsGroupInfoTest {
    private static String account = "1234";
    private static String groupName = "project.group1";
    private static Region groupRegion = Region.US_WEST_1;
    private static Optional<String> encryptorArn = Optional.of("arn:aws:kms:eu-west-1:1234:key/d413a4de-5eb5-4eb4-b4af-373bcba5efdf");
    private static Optional<String> storageArn = Optional.of( "arn:aws:dynamodb:eu-west-1:1234:table/strongbox_eu-west-1_project-group1");
    private static Optional<String> adminArn = Optional.of("arn:aws:iam::1234:policy/strongbox/strongbox_eu-west-1_project-group1_admin");
    private static Optional<String> readonlyArn = Optional.of("arn:aws:iam::1234:policy/strongbox/strongbox_eu-west-1_project-group1_readonly");

    private static List<Principal> getReadOnlyPrincipals() {
        List<Principal> readonlyPrincipals = new ArrayList<Principal>();
        readonlyPrincipals.add(Principal.fromArn("arn:aws:iam::1234:user/bob", "1234"));
        readonlyPrincipals.add(Principal.fromArn("arn:aws:iam::1234:role/awesomeservice", "1234"));
        return readonlyPrincipals;
    }

    private static List<Principal> getAdminPrincipals() {
        List<Principal> adminPrincipals = new ArrayList<Principal>();
        adminPrincipals.add(Principal.fromArn("arn:aws:iam::1234:user/alice", "1234"));
        adminPrincipals.add(Principal.fromArn("arn:aws:iam::1234:group/awesometeam", "1234"));
        return adminPrincipals;
    }

    @Test
    public void testSecretsGroupInfo() {
        List<Principal> readonlyPrincipals = getReadOnlyPrincipals();
        List<Principal> adminPrincipals = getAdminPrincipals();

        SecretsGroupInfo info = new SecretsGroupInfo(
                new SecretsGroupSRN(account, new SecretsGroupIdentifier(groupRegion, groupName)), encryptorArn, storageArn, adminArn, readonlyArn,
                adminPrincipals, readonlyPrincipals);

        assertEquals(info.srn.account, account);
        assertEquals(info.srn.groupIdentifier.name, groupName);
        assertEquals(info.srn.groupIdentifier.region, groupRegion);
        assertEquals(info.encryptorArn, encryptorArn);
        assertEquals(info.storageArn, storageArn);
        assertEquals(info.adminPolicyArn, adminArn);
        assertEquals(info.readOnlyPolicyArn, readonlyArn);
        assertEquals(info.admin, adminPrincipals);
        assertEquals(info.readOnly, readonlyPrincipals);

        assertEquals(
                info.toString(),
                "SecretsGroupInfo{srn=srn:aws:strongbox:us-west-1:1234:group/project/group1, " +
                "storageArn=Optional[arn:aws:dynamodb:eu-west-1:1234:table/strongbox_eu-west-1_project-group1], " +
                "encryptorArn=Optional[arn:aws:kms:eu-west-1:1234:key/d413a4de-5eb5-4eb4-b4af-373bcba5efdf], " +
                "adminPolicyArn=Optional[arn:aws:iam::1234:policy/strongbox/strongbox_eu-west-1_project-group1_admin], " +
                "readOnlyPolicyArn=Optional[arn:aws:iam::1234:policy/strongbox/strongbox_eu-west-1_project-group1_readonly], " +
                "admin=[alice [user], awesometeam [group]], readOnly=[bob [user], awesomeservice [role]]}"
        );
    }

    @Test
    public void testEquals() {
        SecretsGroupInfo info = new SecretsGroupInfo(
                new SecretsGroupSRN(account, new SecretsGroupIdentifier(groupRegion, groupName)), encryptorArn, storageArn, adminArn, readonlyArn,
                new ArrayList<>(), new ArrayList<>());

        // Create SecretInfo with same details.
        SecretsGroupInfo infoCopy = new SecretsGroupInfo(
                new SecretsGroupSRN(account, new SecretsGroupIdentifier(groupRegion, groupName)), encryptorArn, storageArn, adminArn, readonlyArn,
                new ArrayList<>(), new ArrayList<>());
        assertTrue(info.equals(infoCopy));

        // Different region.
        SecretsGroupInfo differentInfo = new SecretsGroupInfo(
                new SecretsGroupSRN(account, new SecretsGroupIdentifier(Region.EU_WEST_1, groupName)), encryptorArn, storageArn, adminArn,
                readonlyArn, new ArrayList<>(), new ArrayList<>());
        assertFalse(info.equals(differentInfo));

        // Different Group name.
        differentInfo = new SecretsGroupInfo(
                new SecretsGroupSRN(account, new SecretsGroupIdentifier(groupRegion, "other.name")), encryptorArn, storageArn, adminArn,
                readonlyArn, new ArrayList<>(), new ArrayList<>());
        assertFalse(info.equals(differentInfo));

        // Different encryptor ARN.
        differentInfo = new SecretsGroupInfo(
                new SecretsGroupSRN(account, new SecretsGroupIdentifier(groupRegion, groupName)), Optional.of("other encryptor ARN"), storageArn,
                adminArn, readonlyArn, new ArrayList<>(), new ArrayList<>());
        assertFalse(info.equals(differentInfo));

        // Different storage ARN.
        differentInfo = new SecretsGroupInfo(
                new SecretsGroupSRN(account, new SecretsGroupIdentifier(groupRegion, groupName)), encryptorArn, Optional.of("other storage ARN"),
                adminArn, readonlyArn, new ArrayList<>(), new ArrayList<>());
        assertFalse(info.equals(differentInfo));

        // Different Admin ARN.
        differentInfo = new SecretsGroupInfo(
                new SecretsGroupSRN(account, new SecretsGroupIdentifier(groupRegion, groupName)), encryptorArn, storageArn,
                Optional.of("other admin ARN"), readonlyArn, new ArrayList<>(), new ArrayList<>());
        assertFalse(info.equals(differentInfo));

        // Different readonly ARN.
        differentInfo = new SecretsGroupInfo(
                new SecretsGroupSRN(account, new SecretsGroupIdentifier(groupRegion, groupName)), encryptorArn, storageArn, adminArn,
                Optional.of("other readonly ARN"), new ArrayList<>(), new ArrayList<>());
        assertFalse(info.equals(differentInfo));

        // Different admin principals.
        differentInfo = new SecretsGroupInfo(
                new SecretsGroupSRN(account, new SecretsGroupIdentifier(groupRegion, groupName)), encryptorArn, storageArn, adminArn,
                readonlyArn, getAdminPrincipals(), new ArrayList<>());
        assertFalse(info.equals(differentInfo));

        // Different readonly principals.
        differentInfo = new SecretsGroupInfo(
                new SecretsGroupSRN(account, new SecretsGroupIdentifier(groupRegion, groupName)), encryptorArn, storageArn, adminArn,
                readonlyArn, new ArrayList<>(), getReadOnlyPrincipals());
        assertFalse(info.equals(differentInfo));

        // Test with some object type, should return False rather than throw an exception.
        assertFalse(info.equals("some string value"));
    }
}
