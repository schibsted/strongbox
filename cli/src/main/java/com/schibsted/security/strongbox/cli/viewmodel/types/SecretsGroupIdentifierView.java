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
