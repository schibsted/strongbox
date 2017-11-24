/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.viewmodel.types;

import java.util.Map;

/**
 * @author stiankri
 */
public interface View {
    Map<String, BinaryString> toMap();

    String uniqueName();

}
