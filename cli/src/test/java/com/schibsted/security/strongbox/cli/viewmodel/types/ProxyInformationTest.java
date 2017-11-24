/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
