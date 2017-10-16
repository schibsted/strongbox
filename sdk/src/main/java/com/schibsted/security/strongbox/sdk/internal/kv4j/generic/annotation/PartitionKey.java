/*
 * Copyright 2016 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.kv4j.generic.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author stiankri
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PartitionKey {
    int position();
    int padding() default 0;
}
