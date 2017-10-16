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

package com.schibsted.security.strongbox.sdk.internal.config;

import com.schibsted.security.strongbox.sdk.types.ProfileIdentifier;
import com.schibsted.security.strongbox.sdk.types.arn.RoleARN;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author stiankri
 */
public class AWSConfigTest {
    private String mfaSerialValue = "arn:aws:iam::12345678910:mfa/john.doe";
    private ProfileIdentifier sourceProfileValue = new ProfileIdentifier("source-profile");
    private RoleARN roleARNValue = new RoleARN("arn:aws:iam::12345678910:role/my-role");
    private String awsAccessKeyIdValue = "AAAA";
    private String awsSecretAccessKeyValue = "1234";

    private AWSCLIConfigFile.Property mfa_serial = new AWSCLIConfigFile.Property("mfa_serial", mfaSerialValue);
    private AWSCLIConfigFile.Property source_profile = new AWSCLIConfigFile.Property("source_profile", sourceProfileValue.name);
    private AWSCLIConfigFile.Property role_arn = new AWSCLIConfigFile.Property("role_arn", roleARNValue.toArn());
    private AWSCLIConfigFile.Property aws_access_key_id = new AWSCLIConfigFile.Property("aws_access_key_id", awsAccessKeyIdValue);
    private AWSCLIConfigFile.Property aws_secret_access_key = new AWSCLIConfigFile.Property("aws_secret_access_key", awsSecretAccessKeyValue);

    private ProfileIdentifier profile = new ProfileIdentifier("my-profile");

    private AWSConfig config;

    @BeforeTest
    public void setup() {
        AWSCLIConfigFile.Section section = new AWSCLIConfigFile.Section(profile.name);
        section.addProperty(mfa_serial);
        section.addProperty(source_profile);
        section.addProperty(role_arn);
        section.addProperty(aws_access_key_id);
        section.addProperty(aws_secret_access_key);

        Map<String, AWSCLIConfigFile.Section> sectionMap = new HashMap<>();
        sectionMap.put(profile.name, section);

        config = new AWSConfig(new AWSCLIConfigFile.Config(sectionMap));
    }

    @Test
    public void get_all_properties() {
        assertThat(config.getMFASerial(profile), is(Optional.of(mfaSerialValue)));
        assertThat(config.getSourceProfile(profile), is(Optional.of(sourceProfileValue)));
        assertThat(config.getRoleArn(profile), is(Optional.of(roleARNValue)));
        assertThat(config.getAWSAccessKeyId(profile), is(Optional.of(awsAccessKeyIdValue)));
        assertThat(config.getAWSSecretKey(profile), is(Optional.of(awsSecretAccessKeyValue)));
    }

    @Test
    public void field_constants() {
        assertThat(AWSConfigPropertyKey.MFA_SERIAL.toString(), is("mfa_serial"));
        assertThat(AWSConfigPropertyKey.SOURCE_PROFILE.toString(), is("source_profile"));
        assertThat(AWSConfigPropertyKey.ROLE_ARN.toString(), is("role_arn"));
        assertThat(AWSConfigPropertyKey.AWS_ACCESS_KEY_ID.toString(), is("aws_access_key_id"));
        assertThat(AWSConfigPropertyKey.AWS_SECRET_ACCESS_KEY.toString(), is("aws_secret_access_key"));
    }
}
