/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.viewmodel.types;

import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;
import com.schibsted.security.strongbox.sdk.types.UserData;

/**
 * @author stiankri
 */
public class UserDataView {
    public final String userData;

    public UserDataView(UserData userData) {
        this.userData = Encoder.base64encode(userData.asByteArray());
    }
}
