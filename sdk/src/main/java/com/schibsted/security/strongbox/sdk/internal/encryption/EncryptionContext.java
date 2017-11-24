/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

import java.util.Map;

/**
 * @author stiankri
 */
public interface EncryptionContext {
    Map<String, String> toMap();
}
