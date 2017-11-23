/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.access;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.ListRolesRequest;
import com.amazonaws.services.identitymanagement.model.ListRolesResult;
import com.amazonaws.services.identitymanagement.model.Role;
import com.schibsted.security.strongbox.sdk.types.Principal;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author stiankri
 * @author hawkaa
 */
public class PrincipalAutoSuggestionTest {
    private AmazonIdentityManagementClient mockClient;
    private PrincipalAutoSuggestion partiallyMockedPrincipalAutoSuggestion;

    @BeforeMethod
    public void setUp() {
        mockClient = mock(AmazonIdentityManagementClient.class);
        PrincipalAutoSuggestion principalAutoSuggestion = new PrincipalAutoSuggestion(mockClient);
        partiallyMockedPrincipalAutoSuggestion= spy(principalAutoSuggestion);
    }

    @Test
    public void testAutoSuggestion() throws Exception {
        ListRolesRequest request = new ListRolesRequest().withMaxItems(1000);

        Role role1 = new Role().withRoleName("foobar1");
        Role role2 = new Role().withRoleName("afoobar");
        Role role3 = new Role().withRoleName("foooobar");
        ListRolesResult mockResult = new ListRolesResult();
        mockResult.withRoles(role1, role2, role3);

        when(mockClient.listRoles(request)).thenReturn(mockResult);
        List<Principal> list = partiallyMockedPrincipalAutoSuggestion.autoSuggestion("foobar");
        assertEquals(list.size(), 2);
        assertEquals(list.get(0).name, "foobar1");
        assertEquals(list.get(1).name, "afoobar");

        verify(mockClient, times(1)).listRoles(request);
    }

    @Test
    public void testAutoSuggestionShortName() throws Exception {
        // Won't call the list method if less than 3 chars.
        ListRolesRequest request = new ListRolesRequest().withMaxItems(1000);
        List<Principal> list = partiallyMockedPrincipalAutoSuggestion.autoSuggestion("fo");
        assertTrue(list.isEmpty());
        verify(mockClient, never()).listRoles(request);
    }

    @Test
    public void testAutoSuggestionCaseInsensitive() throws Exception {
        ListRolesRequest request = new ListRolesRequest().withMaxItems(1000);

        Role lowercase = new Role().withRoleName("foobar");
        Role uppercase = new Role().withRoleName("FOOBAR");
        Role mixedCase = new Role().withRoleName("FooBar");
        ListRolesResult mockResult = new ListRolesResult();
        mockResult.withRoles(lowercase, uppercase, mixedCase);

        when(mockClient.listRoles(request)).thenReturn(mockResult);

        List<Principal> list = partiallyMockedPrincipalAutoSuggestion.autoSuggestion("fOOb");
        assertEquals(list.size(), 3);
        assertEquals(list.get(0).name, "foobar");
        assertEquals(list.get(1).name, "FOOBAR");
        assertEquals(list.get(2).name, "FooBar");
    }
}
