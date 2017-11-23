/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.json;

import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.ZonedDateTime;

/**
 * @author stiankri
 */
public class StrongboxModule extends SimpleModule {
    public StrongboxModule() {
        addSerializer(ZonedDateTime.class, new CustomZonedDateTimeSerializer());
        addDeserializer(ZonedDateTime.class, new CustomZonedDateTimeDeserializer());

        addSerializer(Regions.class, new RegionSerializer());
        addDeserializer(Regions.class, new RegionDeserializer());
    }
}
