/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.viewmodel.types;

import com.schibsted.security.strongbox.sdk.types.UserAlias;

/**
 * @author stiankri
 */
public class UserAliasView {
    public final String alias;

    public UserAliasView(UserAlias userAlias) {
        this.alias = userAlias.alias;
    }

    @Override
    public String toString() {
        return alias;
    }
}
