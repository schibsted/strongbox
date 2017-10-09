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
