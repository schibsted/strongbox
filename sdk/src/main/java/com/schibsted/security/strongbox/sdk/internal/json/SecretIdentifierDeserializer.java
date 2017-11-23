/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;

import java.io.IOException;

/**
 * @author stiankri
 */
public class SecretIdentifierDeserializer extends JsonDeserializer<SecretIdentifier> {
    @Override
    public SecretIdentifier deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return new SecretIdentifier(parser.getValueAsString());
    }
}
