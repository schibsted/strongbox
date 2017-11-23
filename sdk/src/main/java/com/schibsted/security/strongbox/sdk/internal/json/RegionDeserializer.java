/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.json;

import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * @author stiankri
 */
public class RegionDeserializer extends JsonDeserializer<Regions> {
    @Override
    public Regions deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        return Regions.fromName(parser.getValueAsString());
    }
}
