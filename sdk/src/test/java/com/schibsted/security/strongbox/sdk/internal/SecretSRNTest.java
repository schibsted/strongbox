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

package com.schibsted.security.strongbox.sdk.internal;

import com.schibsted.security.strongbox.sdk.exceptions.InvalidResourceName;
import com.schibsted.security.strongbox.sdk.internal.srn.SecretSRN;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import org.hamcrest.MatcherAssert;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.*;

/**
 * @author stiankri
 * @author kvlees
 */
public class SecretSRNTest {

    private SecretSRN secretSRN = new SecretSRN(
            "1234", new SecretsGroupIdentifier(Region.EU_WEST_1, "team.project"), new SecretIdentifier("MySecret"));
    private String srn = "srn:aws:strongbox:eu-west-1:1234:secret/team/project/MySecret";

    @Test
    public void serialize() {
        String serialized = secretSRN.toSrn();
        assertThat(serialized, is(srn));
    }

    @Test
    public void deserialize() {
        SecretSRN deserialized = SecretSRN.fromSrn(srn);
        assertThat(deserialized, is(secretSRN));
        assertThat(deserialized.account, is("1234"));
        assertThat(deserialized.groupIdentifier.region, is(Region.EU_WEST_1));
        assertThat(deserialized.groupIdentifier.name, is("team.project"));
        MatcherAssert.assertThat(deserialized.secretIdentifier.name, is("MySecret"));
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void deserializeInvalidSrn() throws Exception {
        SecretSRN.fromSrn("arn:aws:strongbox:eu-west-1:1234:secret/team/project/MySecret");
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void deserializeInvalidPrefix() throws Exception {
        SecretSRN.fromSrn("srn:aws:strongbox:eu-west-1:1234:invalid/team/project/MySecret");
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void deserializeMissingRegion() throws Exception {
        SecretSRN.fromSrn("srn:aws:strongbox:1234:secret/team/project/MySecret");
    }

    @Test
    public void testEquals() throws Exception {
        SecretSRN sameSRN = new SecretSRN(
                "1234", new SecretsGroupIdentifier(Region.EU_WEST_1, "team.project"), new SecretIdentifier("MySecret"));
        assertTrue(secretSRN.equals(sameSRN));

        // Different account.
        SecretSRN differentSRN = new SecretSRN(
                "6789", new SecretsGroupIdentifier(Region.EU_WEST_1, "team.project"), new SecretIdentifier("MySecret"));
        assertFalse(secretSRN.equals(differentSRN));

        // Different region.
        differentSRN = new SecretSRN(
                "1234", new SecretsGroupIdentifier(Region.US_WEST_1, "team.project"), new SecretIdentifier("MySecret"));
        assertFalse(secretSRN.equals(differentSRN));

        // Different group name.
        differentSRN = new SecretSRN(
                "1234", new SecretsGroupIdentifier(Region.EU_WEST_1, "other.project"), new SecretIdentifier("MySecret"));
        assertFalse(secretSRN.equals(differentSRN));

        // Different secret name.
        differentSRN = new SecretSRN(
                "1234", new SecretsGroupIdentifier(Region.EU_WEST_1, "team.project"),
                new SecretIdentifier("OtherSecret"));
        assertFalse(secretSRN.equals(differentSRN));
    }
}
