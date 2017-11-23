/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.config;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.internal.AwsProfileNameLoader;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.amazonaws.util.EC2MetadataUtils;
import com.schibsted.security.strongbox.sdk.exceptions.FailedToResolveRegionException;
import com.schibsted.security.strongbox.sdk.types.ProfileIdentifier;
import com.schibsted.security.strongbox.sdk.types.Region;

import java.io.File;
import java.util.Optional;

/**
 * @author stiankri
 */
public class CustomRegionProviderChain {

    public Region resolveRegion() {
        return resolveRegion(Optional.empty(), new ProfileIdentifier(AwsProfileNameLoader.DEFAULT_PROFILE_NAME));
    }

    // https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-region-selection.html#automatically-determine-the-aws-region-from-the-environment
    public Region resolveRegion(Optional<String> region, ProfileIdentifier profile) {
        Optional<Region> resolvedRegion = Optional.empty();

        if (region.isPresent()) {
            resolvedRegion = Optional.of(Region.fromName(region.get()));
        }
        if (!resolvedRegion.isPresent()){
            resolvedRegion = getRegionFromEnvironment();
        }
        if (!resolvedRegion.isPresent()){
            resolvedRegion = getRegionFromProfile(profile);
        }
        if (!resolvedRegion.isPresent()){
            resolvedRegion = getRegionFromMetadata();
        }

        if (!resolvedRegion.isPresent()) {
            throw new FailedToResolveRegionException("A region must be specified");
        }

        return resolvedRegion.get();
    }

    private Optional<Region> getRegionFromEnvironment() {
        Region resolvedRegion = null;
        if (System.getenv("AWS_REGION") != null) {
            resolvedRegion = Region.fromName(System.getenv("AWS_REGION"));
        } else if (System.getenv("AWS_DEFAULT_REGION") != null) {
            resolvedRegion = Region.fromName(System.getenv("AWS_DEFAULT_REGION"));
        }
        return Optional.ofNullable(resolvedRegion);
    }

    private Optional<Region> getRegionFromProfile(ProfileIdentifier profile) {
        return getDefaultRegionFromConfigFile(profile.name);
    }

    private Optional<Region> getRegionFromMetadata() {
        try {
            Region resolvedRegion = null;
            if (EC2MetadataUtils.getInstanceInfo() != null) {
                if (EC2MetadataUtils.getInstanceInfo().getRegion() != null) {
                    resolvedRegion = Region.fromName(EC2MetadataUtils.getInstanceInfo().getRegion());
                } else { // fallback to provider chain if region is not exposed
                    resolvedRegion = Region.fromName(new DefaultAwsRegionProviderChain().getRegion());
                }
            }
            return Optional.ofNullable(resolvedRegion);
        } catch (SdkClientException e) {
            return Optional.empty();
        }
    }

    private Optional<Region> getDefaultRegionFromConfigFile(String profile) {

        Optional<String> region = AWSCLIConfigFile.getCredentialProfilesFile()
                .flatMap(file -> getRegionFromConfigFile(file, profile));

        if (!region.isPresent()) {
            region = AWSCLIConfigFile.getConfigFile()
                    .flatMap(file -> getRegionFromConfigFile(file, profile));
        }

        return region.map(Region::fromName);
    }

    private static Optional<String> getRegionFromConfigFile(File file, String profile) {
        AWSCLIConfigFile configFile = new AWSCLIConfigFile(file);
        AWSCLIConfigFile.Config config = configFile.getConfig();

        Optional<AWSCLIConfigFile.Section> profileSection = config.getSection(profile);

        // Legacy fallback
        if (!profileSection.isPresent()) {
            profileSection = config.getSection("profile " + profile);
        }

        return profileSection.flatMap(s -> s.getProperty("region"));
    }
}
