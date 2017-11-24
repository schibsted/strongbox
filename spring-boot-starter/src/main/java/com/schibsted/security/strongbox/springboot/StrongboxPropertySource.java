/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.springboot;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.schibsted.security.strongbox.sdk.SimpleSecretsGroup;
import org.springframework.core.env.EnumerablePropertySource;


public class StrongboxPropertySource extends EnumerablePropertySource<Map<String, Object>> {
    private Map<String, Object> source = new LinkedHashMap<>();

    public StrongboxPropertySource(StrongboxBootstrapConfiguration configuration, SimpleSecretsGroup simpleSecretsGroup) {
        super("strongbox-property-source-" + configuration.getGroupname());
        List<StrongboxNameToPropertyName> strongboxToPropertyMapping = configuration.getProperties();
        if (strongboxToPropertyMapping.size() != 0) {
            strongboxToPropertyMapping.forEach(mapping -> mapProperty(simpleSecretsGroup, mapping));
        } else {
            simpleSecretsGroup.getAllStringSecrets().forEach(secret -> setProperty(secret.secretIdentifer.name, secret.value, secret.version));
        }
    }

    private Object mapProperty(SimpleSecretsGroup simpleSecretsGroup, StrongboxNameToPropertyName map) {
        String secret = simpleSecretsGroup
                .getStringSecret(map.getStrongboxName())
                .orElseThrow(
                        () -> new IllegalStateException(String.format("Secret with name %s cannot be found in Strongbox", map.getStrongboxName()))
                );

        return setProperty(map.getPropertyName(), secret);
    }

    private Object setProperty(String name, String value) {
        logger.debug("Setting property '" + name + "' from Strongbox");
        return source.put(name, value);
    }

    private Object setProperty(String name, String value, long version) {
        logger.debug("Setting property '" + name + "' from Strongbox, version " + String.valueOf(version));
        return source.put(name, value);
    }

    @Override
    public String[] getPropertyNames() {
        return source.keySet().toArray(new String[source.size()]);
    }

    @Override
    public Object getProperty(String name) {
        return source.get(name);
    }
}
