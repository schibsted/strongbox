/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.interfaces;

import java.util.Optional;

public interface ManagedResource {
    String create();
    void delete();

    Optional<String> awsAdminPolicy();
    Optional<String> awsReadOnlyPolicy();
    String getArn();

    boolean exists();
}
