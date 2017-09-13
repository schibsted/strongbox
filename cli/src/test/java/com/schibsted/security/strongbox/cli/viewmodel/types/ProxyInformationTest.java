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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author stiankri
 * @author torarvid
 */
public class ProxyInformationTest {
    @Test
    public void should_parse_username_and_password() {
        ProxyInformation pi = new ProxyInformation("http://user:pass@proxy.internal:3128", Optional.empty());
        assertThat(pi.username, is(Optional.of("user")));
        assertThat(pi.password, is(Optional.of("pass")));
        assertThat(pi.host, is("proxy.internal"));
        assertThat(pi.port, is(3128));
    }

    @Test
    public void should_parse_host_and_port() {
        ProxyInformation pi = new ProxyInformation("http://proxy.internal:3128", Optional.empty());
        assertThat(pi.username, is(Optional.empty()));
        assertThat(pi.password, is(Optional.empty()));
        assertThat(pi.host, is("proxy.internal"));
        assertThat(pi.port, is(3128));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void should_fail_on_malformed_username_password() {
        new ProxyInformation("http://userpass@proxy.internal:3128", Optional.empty());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void should_fail_on_malformed_host_port() {
        new ProxyInformation("http://user:pass@proxy.internal:a3128", Optional.empty());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void should_fail_on_malformed_protocol() {
        new ProxyInformation("httpfoo://user:pass@proxy.internal:3128", Optional.empty());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void should_fail_on_missing_password() {
        new ProxyInformation("httpfoo://user:@proxy.internal:3128", Optional.empty());
    }

    @Test
    public void should_parse_no_proxy() {
        ProxyInformation pi = new ProxyInformation("http://proxy.internal:3128", Optional.of("foo, .other.com"));
        assertEquals(pi.nonProxyHosts, Arrays.asList("foo", "*.other.com"));
    }
}
