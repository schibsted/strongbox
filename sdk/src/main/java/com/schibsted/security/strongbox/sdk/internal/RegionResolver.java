/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal;

import com.amazonaws.regions.Regions;
import com.schibsted.security.strongbox.sdk.exceptions.FailedToResolveRegionException;
import com.schibsted.security.strongbox.sdk.internal.config.CustomRegionProviderChain;
import com.schibsted.security.strongbox.sdk.types.Region;

import java.util.Optional;

/**
 * This resolver is to maintain backwards compatibility from before
 * the AWS SDK required us to specify a region.
 *
 * @author stiankri
 */
public class RegionResolver {
    private static Optional<Region> userSetRegion = Optional.empty();
    private static Optional<Region> cachedRegion = Optional.empty();

    public static String getRegion() {
        if (userSetRegion.isPresent()) {
            return userSetRegion.get().getName();
        }

        if (cachedRegion.isPresent()) {
            return cachedRegion.get().getName();
        }

        CustomRegionProviderChain regionProvider = new CustomRegionProviderChain();
        try {
            Region region = regionProvider.resolveRegion();

            cachedRegion = Optional.of(region);

            return region.getName();
        } catch (FailedToResolveRegionException e) {
            return Regions.DEFAULT_REGION.getName();
        }
    }

    public static void setRegion(Region region) {
        userSetRegion = Optional.of(region);
    }
}
