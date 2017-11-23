/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import com.google.common.base.Joiner;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a copy of AWS' Regions class to avoid exposing AWS classes in the Strongbox API
 */
public enum Region {

    GovCloud("us-gov-west-1"),
    US_EAST_1("us-east-1"),
    US_WEST_1("us-west-1"),
    US_WEST_2("us-west-2"),
    EU_WEST_1("eu-west-1"),
    EU_CENTRAL_1("eu-central-1"),
    AP_SOUTHEAST_1("ap-southeast-1"),
    AP_SOUTHEAST_2("ap-southeast-2"),
    AP_NORTHEAST_1("ap-northeast-1"),
    AP_NORTHEAST_2("ap-northeast-2"),
    SA_EAST_1("sa-east-1"),
    CN_NORTH_1("cn-north-1"),
    ;

    private static Map<String, Region> regionMap = new HashMap();

    static {
        for (Region region : values()) {
            regionMap.put(region.name, region);
        }
    }

    public final String name;

    Region(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Region fromName(String regionName) {
        Region region = regionMap.get(regionName);

        if (region == null) {
            throw new IllegalArgumentException(String.format("No region called '%s', expected one of {%s}",
                    regionName, Joiner.on(", ").join(regionMap.keySet())));
        }

        return region;
    }
}
