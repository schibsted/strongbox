/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import java.util.List;
import java.util.Optional;

/**
 * @author torarvid
 */
public class ClientConfiguration {
    public final Optional<Proxy> proxy;

    public ClientConfiguration() {
        this.proxy = Optional.empty();
    }

    public ClientConfiguration(Proxy proxy) {
        this.proxy = Optional.of(proxy);
    }

    public static class Proxy {
        public final Optional<String> proxyUsername;
        public final Optional<String> proxyPassword;
        public final List<String> nonProxyHosts;
        public final String proxyHost;
        public final int proxyPort;

        public Proxy(Optional<String> proxyUsername, Optional<String> proxyPassword, List<String> nonProxyHosts, String proxyHost, int proxyPort) {
            this.proxyUsername = proxyUsername;
            this.proxyPassword = proxyPassword;
            this.nonProxyHosts = nonProxyHosts;
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
        }
    }
}
