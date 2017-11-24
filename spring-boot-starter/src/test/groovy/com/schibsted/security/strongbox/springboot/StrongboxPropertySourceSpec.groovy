/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.springboot;

import com.schibsted.security.strongbox.sdk.types.SecretIdentifier
import com.schibsted.security.strongbox.sdk.types.StringSecretEntry
import spock.lang.Specification

import com.schibsted.security.strongbox.sdk.SimpleSecretsGroup;


class StrongboxPropertySourceSpec extends Specification {

    def "should set secret based on mapping"() {
        given:
        def configuration = Mock(StrongboxBootstrapConfiguration)
        configuration.getProperties() >> [new StrongboxNameToPropertyName("strongboxName", "propName")]

        def simpleSecretsGroup = Mock(SimpleSecretsGroup)
        simpleSecretsGroup.getStringSecret("strongboxName") >> Optional.of("topsecret")

        when:
        def propertySource = new StrongboxPropertySource(configuration, simpleSecretsGroup)

        then:
        !propertySource.getProperty("nonexistent")
        !propertySource.getProperty("anotherSecret")
        propertySource.getProperty("propName") == 'topsecret'
    }

    def "should set all secrets based on Strongbox secret name"() {
        given:
        def configuration = Mock(StrongboxBootstrapConfiguration)
        configuration.getProperties() >> Collections.emptyList()

        def simpleSecretsGroup = Mock(SimpleSecretsGroup)
        simpleSecretsGroup.getAllStringSecrets() >> [new StringSecretEntry(new SecretIdentifier('camel4Case'), 1l,'topsecret'), new StringSecretEntry(new SecretIdentifier('another.secret.revealed'), 1l,'!@#$%@#$')]

        when:
        def propertySource = new StrongboxPropertySource(configuration, simpleSecretsGroup)

        then:
        !propertySource.getProperty("nonexistent")

        and:
        propertySource.getProperty("camel4Case") == 'topsecret'
        propertySource.getProperty("another.secret.revealed") == '!@#$%@#$'
    }

    def "should throw exception on missing secret from mapping"() {
        given:
            def configuration = Mock(StrongboxBootstrapConfiguration)
            configuration.getProperties() >> [new StrongboxNameToPropertyName("strongboxName", "propName")]
            def simpleSecretsGroup = Mock(SimpleSecretsGroup)
            simpleSecretsGroup.getStringSecret("strongboxName") >> Optional.empty()
        when:
            new StrongboxPropertySource(configuration, simpleSecretsGroup)
        then:
            IllegalStateException ex = thrown()
            assert ex.getMessage() == "Secret with name strongboxName cannot be found in Strongbox"
    }
}
