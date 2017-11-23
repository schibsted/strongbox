/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal;

import com.schibsted.security.strongbox.sdk.internal.srn.SecretsGroupSRN;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author stiankri
 */
public class SecretsGroupSRNTest {

    @Test
    public void toSRN() {
        SecretsGroupSRN srn = new SecretsGroupSRN("1234", new SecretsGroupIdentifier(Region.EU_WEST_1, "team.project"));

        assertThat(srn.toSrn(), is("srn:aws:strongbox:eu-west-1:1234:group/team/project"));
    }
}
