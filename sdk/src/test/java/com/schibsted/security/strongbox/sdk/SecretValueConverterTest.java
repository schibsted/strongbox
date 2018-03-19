/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk;

import com.schibsted.security.strongbox.sdk.internal.converter.SecretValueConverter;
import org.testng.annotations.Test;

import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author jwarlander
 */
public class SecretValueConverterTest {
    @Test
    public void chars_to_bytes() {
        String str = "beeboopfoobarblahblahthisisalongstringyeah";
        char[] charsFromString = str.toCharArray();
        byte[] bytesFromString = str.getBytes(Charset.forName("UTF-8"));
        assertThat(SecretValueConverter.asBytes(charsFromString), is(bytesFromString));

        // Our initial char array above should be shredded now; eg. only nulls
        char[] emptyCharArray = new char[bytesFromString.length];
        assertThat(charsFromString, is(emptyCharArray));
    }
}
