/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.json;

import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author stiankri
 */
public class RegionSerializer extends JsonSerializer<Regions> {
    @Override
    public void serialize(Regions value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        jgen.writeString(value.getName());
    }
}
