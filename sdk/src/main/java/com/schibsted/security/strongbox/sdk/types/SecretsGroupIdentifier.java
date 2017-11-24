/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.util.regex.Pattern;

/**
 * @author stiankri
 */
public final class SecretsGroupIdentifier implements Comparable<SecretsGroupIdentifier> {
    @JsonProperty("region")
    public final Region region;

    @JsonProperty("name")
    public final String name;

    private static final int NAME_MIN_LENGTH = 3;
    private static final int NAME_MAX_LENGTH = 64;
    private static final String NAME_REGEX = "^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*$";
    private static Pattern pattern = Pattern.compile(NAME_REGEX);

    public SecretsGroupIdentifier(@JsonProperty("region") Region region,
                                  @JsonProperty("name") String name) {
        if (name.length() < NAME_MIN_LENGTH) {
            throw new IllegalArgumentException(String.format("The group name '%s' must be at least %d characters long", name, NAME_MIN_LENGTH));
        }

        if (name.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("The group name '%s' can at most be %d characters long", name, NAME_MAX_LENGTH));
        }

        if (!pattern.matcher(name).find()) {
            throw new IllegalArgumentException(String.format("The group name '%s' must match the regular expression '%s'", name, NAME_REGEX));
        }

        this.region = region;
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", name, region.getName());
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
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(SecretsGroupIdentifier o) {
        if (region.compareTo(o.region) == 0) {
            return name.compareTo(o.name);
        } else {
            return region.compareTo(o.region);
        }
    }
}
