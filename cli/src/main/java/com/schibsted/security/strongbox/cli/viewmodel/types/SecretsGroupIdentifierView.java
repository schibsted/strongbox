/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.viewmodel.types;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;

import java.util.Map;

/**
 * @author stiankri
 */
public class SecretsGroupIdentifierView implements View {
    public final String region;
    public final String name;

    public SecretsGroupIdentifierView(SecretsGroupIdentifier group) {
        this.region = group.region.getName();
        this.name = group.name;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", name, region);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, region);
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof SecretsGroupIdentifier){
            final SecretsGroupIdentifier other = (SecretsGroupIdentifier) obj;
            return Objects.equal(name, other.name) && Objects.equal(region, other.region);
        } else{
            return false;
        }
    }

    @Override
    public Map<String, BinaryString> toMap() {
        ImmutableMap.Builder<String, BinaryString> builder = ImmutableMap.builder();
        builder.put("region", new BinaryString(region));
        builder.put("name", new BinaryString(name));
        return builder.build();
    }

    @Override
    public String uniqueName() {
        return String.format("%s.%s", name, region);
    }
}
