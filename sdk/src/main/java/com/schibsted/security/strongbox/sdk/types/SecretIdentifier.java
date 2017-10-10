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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Objects;
import com.schibsted.security.strongbox.sdk.internal.json.SecretIdentifierDeserializer;
import com.schibsted.security.strongbox.sdk.internal.json.SecretIdentifierSerializer;

import java.util.regex.Pattern;

/**
 * @author stiankri
 * @author hawkaa
 */
@JsonSerialize(using = SecretIdentifierSerializer.class)
@JsonDeserialize(using = SecretIdentifierDeserializer.class)
public final class SecretIdentifier {
    public final String name;

    private static final int NAME_MIN_LENGTH = 1;
    private static final int NAME_MAX_LENGTH = 128;
    private static final String NAME_REGEX = "^[a-zA-Z0-9]*([_\\-.][a-zA-Z0-9]+)*$";
    private static Pattern pattern = Pattern.compile(NAME_REGEX);

    @JsonCreator
    public SecretIdentifier(@JsonProperty("name") String name) {
        if (name.length() < NAME_MIN_LENGTH) {
            throw new IllegalArgumentException(String.format("The secret name '%s' must be at least %d characters long", name, NAME_MIN_LENGTH));
        }

        if (name.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("The secret name '%s' cannot be longer than %d characters", name, NAME_MAX_LENGTH));
        }

        if (!pattern.matcher(name).find()) {
            throw new IllegalArgumentException(String.format("The secret name '%s' did not match the regular expression '%s'", name, NAME_REGEX));
        }

        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof SecretIdentifier){
            final SecretIdentifier other = (SecretIdentifier) obj;
            return Objects.equal(name, other.name);
        } else{
            return false;
        }
    }
}
