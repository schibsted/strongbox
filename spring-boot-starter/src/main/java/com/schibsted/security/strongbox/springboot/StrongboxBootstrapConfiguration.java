/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.springboot;

import com.amazonaws.regions.Regions;
import com.schibsted.security.strongbox.sdk.SimpleSecretsGroup;
import com.schibsted.security.strongbox.sdk.impl.DefaultSimpleSecretsGroup;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;


@Configuration
@ConfigurationProperties(prefix = "strongbox")
public class StrongboxBootstrapConfiguration {

    private String groupname;
    private List<StrongboxNameToPropertyName> properties = new ArrayList<>();
    private static final Logger LOG = LoggerFactory.getLogger(StrongboxBootstrapConfiguration.class);

    @Bean
    @ConditionalOnProperty(value = "strongbox.enabled", havingValue = "true")
    public SimpleSecretsGroup getSimpleSecretsGroup() {
        return new DefaultSimpleSecretsGroup(new SecretsGroupIdentifier(getRegion(), groupname));
    }

    public void setGroupname(String name) {
        this.groupname = name;
    }

    public String getGroupname()
    {
        return this.groupname;
    }

    public List<StrongboxNameToPropertyName> getProperties() {
        return properties;
    }

    public void setProperties(List<StrongboxNameToPropertyName> properties) {
        this.properties = properties;
    }

    private com.schibsted.security.strongbox.sdk.types.Region getRegion() {
        com.amazonaws.regions.Region region = Regions.getCurrentRegion();
        if (region == null) {
            LOG.debug("Cannot get current AWS region, using default region eu-west-1");
            region = com.amazonaws.regions.Region.getRegion(Regions.EU_WEST_1);
        }
        return com.schibsted.security.strongbox.sdk.types.Region.fromName(region.getName());
    }
}
