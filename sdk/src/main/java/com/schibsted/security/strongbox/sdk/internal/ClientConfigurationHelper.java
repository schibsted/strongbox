/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal;

import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.amazonaws.Protocol;
import com.schibsted.security.strongbox.sdk.exceptions.SecurityConfigurationException;

/**
 * @author torarvid
 */
public class ClientConfigurationHelper {
    public static com.amazonaws.ClientConfiguration transformAndVerifyOrThrow(ClientConfiguration clientConfiguration) {
        com.amazonaws.ClientConfiguration awsClientConfig = new com.amazonaws.ClientConfigurationFactory().getConfig();
        if (awsClientConfig.getProtocol() != Protocol.HTTPS) {
            throw new SecurityConfigurationException("Must use HTTPS protocol");
        }
        clientConfiguration.proxy.ifPresent(p -> {
            awsClientConfig.setProxyHost(p.proxyHost);
            awsClientConfig.setProxyPort(p.proxyPort);
            if (!p.nonProxyHosts.isEmpty()) {
                awsClientConfig.setNonProxyHosts(String.join("|", p.nonProxyHosts));
            }
            p.proxyUsername.ifPresent(awsClientConfig::setProxyUsername);
            p.proxyPassword.ifPresent(awsClientConfig::setProxyPassword);
        });
        return verifyOrThrow(awsClientConfig);
    }

    public static com.amazonaws.ClientConfiguration verifyOrThrow(com.amazonaws.ClientConfiguration clientConfiguration) {
        if (clientConfiguration.getProtocol() != Protocol.HTTPS) {
            throw new SecurityConfigurationException("Must use HTTPS protocol");
        }
        return clientConfiguration;
    }
}
