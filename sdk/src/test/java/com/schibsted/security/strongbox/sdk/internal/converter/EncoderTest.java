package com.schibsted.security.strongbox.sdk.internal.converter;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author stiankri
 */
public class EncoderTest {
    @Test
    public void asUTF8() {
        assertThat(Encoder.asUTF8("aB"), is(new byte[]{97, 66}));
    }

    @Test
    public void fromUTF8() {
        assertThat(Encoder.fromUTF8(new byte[]{97, 66}), is("aB"));
    }

    @Test
    public void base64_encode() {
        assertThat(Encoder.base64encode(new byte[]{65}), is("QQ=="));
    }

    @Test
    public void base64_decode() {
        assertThat(Encoder.base64decode("QQ=="), is(new byte[]{65}));
    }

    @Test
    public void sha1() {
        assertThat(Encoder.sha1(Encoder.asUTF8("test")), is("qUqP5cyxm6YcTAhz05Hph5gvu9M="));
    }
}
