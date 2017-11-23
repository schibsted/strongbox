/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.config.credentials;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.schibsted.security.strongbox.sdk.internal.RegionResolver;
import com.schibsted.security.strongbox.sdk.internal.config.AWSConfigPropertyKey;
import com.schibsted.security.strongbox.sdk.internal.config.ConfigProviderChain;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.schibsted.security.strongbox.sdk.types.ProfileIdentifier;
import com.schibsted.security.strongbox.sdk.types.arn.RoleARN;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import static com.schibsted.security.strongbox.sdk.internal.ClientConfigurationHelper.transformAndVerifyOrThrow;

/**
 * @author stiankri
 */
public class ProfileCredentialProvider implements AWSCredentialsProvider {
    private ClientConfiguration clientConfiguration;
    private ProfileIdentifier profile;
    private Supplier<MFAToken> mfaTokenSupplier;

    public ProfileCredentialProvider(ClientConfiguration clientConfiguration, ProfileIdentifier profile, Supplier<MFAToken> mfaTokenSupplier) {
        this.clientConfiguration = clientConfiguration;
        this.profile = profile;
        this.mfaTokenSupplier = mfaTokenSupplier;
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

            String sessionId = String.format("strongbox-cli-session-%s", ZonedDateTime.now().toEpochSecond());

            AssumeRoleRequest request = new AssumeRoleRequest();
            request.withRoleArn(roleToAssume.toArn())
                    .withRoleSessionName(sessionId);

            Optional<String> mfaSerial = configProvider.getMFASerial(profile);
            if (mfaSerial.isPresent()) {
                MFAToken mfaToken = mfaTokenSupplier.get();

                request.withSerialNumber(mfaSerial.get())
                        .withTokenCode(mfaToken.value);
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
