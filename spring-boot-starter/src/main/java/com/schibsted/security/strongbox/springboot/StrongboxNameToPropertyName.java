/*
 * Copyright 2017 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.security.strongbox.springboot;


public class StrongboxNameToPropertyName {
    private String strongboxName;
    private String propertyName;

    public StrongboxNameToPropertyName(String strongboxName, String propertyName) {
        this.strongboxName = strongboxName;
        this.propertyName = propertyName;
    }
    public StrongboxNameToPropertyName() {}

    public String getStrongboxName() {
        return strongboxName;
    }

    public void setStrongboxName(String strongboxName) {
        this.strongboxName = strongboxName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
