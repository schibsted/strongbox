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

import com.amazonaws.SdkClientException;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.amazonaws.regions.Regions;
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

    public static String getRegion() {
        if (userSetRegion.isPresent()) {
            return userSetRegion.get().getName();
        }

        DefaultAwsRegionProviderChain providerChain = new DefaultAwsRegionProviderChain();
        try {
            return providerChain.getRegion();
        } catch (SdkClientException e) {
            return Regions.DEFAULT_REGION.getName();
        }
    }

    public static void setRegion(Region region) {
        userSetRegion = Optional.of(region);
    }
}
