/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.springboot;

import com.schibsted.security.strongbox.sdk.SimpleSecretsGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnProperty(name = "strongbox.enabled", havingValue = "true")
public class StrongboxPropertySourceLocator implements PropertySourceLocator {

    @Autowired
    private StrongboxBootstrapConfiguration configuration;

    @Autowired
    private SimpleSecretsGroup simpleSecretsGroup;

    @Override
    public org.springframework.core.env.PropertySource locate(org.springframework.core.env.Environment environment) {
        return new StrongboxPropertySource(configuration, simpleSecretsGroup);
    }

}
