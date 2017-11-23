/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.viewmodel.types;

import com.schibsted.security.strongbox.sdk.types.Principal;

/**
 * @author stiankri
 */
public class PrincipalView {
    public final String type;
    public final String name;

    public PrincipalView(Principal principal) {
        this.type = principal.type.name;
        this.name = principal.name;
    }
}
