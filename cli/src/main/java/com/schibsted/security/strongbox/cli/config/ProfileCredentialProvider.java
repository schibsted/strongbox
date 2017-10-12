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

package com.schibsted.security.strongbox.cli.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.identitymanagement.model.InvalidInputException;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.schibsted.security.strongbox.cli.mfa.SessionCache;
import com.schibsted.security.strongbox.sdk.internal.RegionResolver;
import com.schibsted.security.strongbox.sdk.internal.config.AWSConfigPropertyKey;
import com.schibsted.security.strongbox.sdk.internal.config.ConfigProviderChain;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.schibsted.security.strongbox.sdk.types.ProfileIdentifier;
import com.schibsted.security.strongbox.sdk.types.arn.RoleARN;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.schibsted.security.strongbox.sdk.internal.ClientConfigurationHelper.transformAndVerifyOrThrow;

/**
 * @author stiankri
 */
public class ProfileCredentialProvider implements AWSCredentialsProvider {
    private ClientConfiguration clientConfiguration;
    private ProfileIdentifier profile;

    public ProfileCredentialProvider(ClientConfiguration clientConfiguration, ProfileIdentifier profile) {
        this.clientConfiguration = clientConfiguration;
        this.profile = profile;
    }

    @Override
    public AWSCredentials getCredentials() {
        return getCredentialsFromProfile(profile);
    }

    @Override
    public void refresh() {

    }

    private AWSCredentials getCredentialsFromProfile(final ProfileIdentifier profile) {
        ConfigProviderChain configProvider = new ConfigProviderChain();
        if (!configProvider.hasConfig()) {
            throw new IllegalStateException("When using '--profile', an AWS credentials or config file must be present");
        }

        Optional<RoleARN> roleToAssume = configProvider.getRoleArn(profile);
        if (roleToAssume.isPresent()) {
            return assumeRole(clientConfiguration, configProvider, profile, roleToAssume.get());
        } else {
            return getStaticCredentials(configProvider, profile);
        }
    }

    private Optional<ProfileIdentifier> resolveProfile(Optional<ProfileIdentifier> profile) {
        String awsProfile = System.getenv("AWS_PROFILE");
        String awsDefaultProfile = System.getenv("AWS_DEFAULT_PROFILE");

        if (!profile.isPresent()) {
            if (awsProfile != null) {
                profile = Optional.ofNullable(awsProfile).map(ProfileIdentifier::new);
            }
            if (awsDefaultProfile != null) {
                profile = Optional.ofNullable(awsDefaultProfile).map(ProfileIdentifier::new);
            }
        }

        return profile;
    }

    /**
     * Resolve AWS credentials based on MFA/Assume role
     *
     * We will assume that if mfa_serial is defined, then role_arn and source_profile also has to be specified.
     *
     * Please note that Strongbox differ from the AWS CLI in the following:
     * AWS CLI: 'Note that configuration variables for using IAM roles can only be in the AWS CLI config file.'
     * Strongbox: '--assume-role' can be specified explicitly
     *
     * https://docs.aws.amazon.com/cli/latest/topic/config-vars.html#using-aws-iam-roles
     */
    private AWSCredentials assumeRole(ClientConfiguration clientConfiguration,
                                              ConfigProviderChain configProvider,
                                              ProfileIdentifier profile,
                                              RoleARN roleToAssume) {

        Optional<ProfileIdentifier> sourceProfile = configProvider.getSourceProfile(profile);
        if (!sourceProfile.isPresent()) {
            throw new IllegalStateException(String.format("'%s' must be specified when using '%s' for profile '%s'",
                    AWSConfigPropertyKey.SOURCE_PROFILE,
                    AWSConfigPropertyKey.ROLE_ARN,
                    profile.name));
        }

        SessionCache sessionCache = new SessionCache(profile, roleToAssume);
        Optional<BasicSessionCredentials> cachedCredentials = sessionCache.load();

        if (cachedCredentials.isPresent()) {
            return cachedCredentials.get();
        } else {
            AWSCredentialsProvider staticCredentialsProvider = new AWSStaticCredentialsProvider(getStaticCredentials(configProvider, sourceProfile.get()));

            AWSSecurityTokenService client = AWSSecurityTokenServiceClientBuilder.standard()
                    .withCredentials(staticCredentialsProvider)
                    .withClientConfiguration(transformAndVerifyOrThrow(clientConfiguration))
                    .withRegion(RegionResolver.getRegion())
                    .build();

            Optional<String> mfaSerial = configProvider.getMFASerial(profile);
            String token = "";
            if (mfaSerial.isPresent()) {
                char[] secretValue = System.console().readPassword("Enter MFA code: ");
                if (secretValue == null || secretValue.length == 0) {
                    throw new InvalidInputException("A non-empty MFA code must be entered");
                }
                token = new String(secretValue);
            }

            String sessionId = String.format("strongbox-cli-session-%s", ZonedDateTime.now().toEpochSecond());

            AssumeRoleRequest request = new AssumeRoleRequest();
            request.withRoleArn(roleToAssume.toArn())
                    .withRoleSessionName(sessionId);

            if (mfaSerial.isPresent()) {
                request.withSerialNumber(mfaSerial.get())
                        .withTokenCode(token);
            }

            AssumeRoleResult result = client.assumeRole(request);
            Credentials credentials = result.getCredentials();

            BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(credentials.getAccessKeyId(), credentials.getSecretAccessKey(), credentials.getSessionToken());

            sessionCache.save(result.getAssumedRoleUser(),
                    basicSessionCredentials,
                    ZonedDateTime.ofInstant(credentials.getExpiration().toInstant(), ZoneId.of("UTC")));

            return basicSessionCredentials;
        }
    }

    private AWSCredentials getStaticCredentials(ConfigProviderChain configProviderChain, ProfileIdentifier profile) {
        return new BasicAWSCredentials(configProviderChain.getAWSAccessKeyIdOrThrow(profile), configProviderChain.getAWSSecretKeyOrThrow(profile));
    }
}
