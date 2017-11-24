/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.gui;

import com.schibsted.security.strongbox.sdk.internal.access.PrincipalAutoSuggestion;
import com.schibsted.security.strongbox.sdk.internal.encryption.RandomGenerator;
import com.schibsted.security.strongbox.sdk.SecretsGroupManager;
import com.schibsted.security.strongbox.sdk.types.Region;

/**
 * @author stiankri
 */
public class Singleton {
    public static SecretsGroupManager secretsGroupManager;
    public static Region region;
    public static RandomGenerator randomGenerator;
    public static PrincipalAutoSuggestion principalAutoSuggestion;
}
