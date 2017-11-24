/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.viewmodel.types;

import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;
import com.schibsted.security.strongbox.sdk.types.Encoding;
import com.schibsted.security.strongbox.sdk.types.SecretValue;

/**
 * @author stiankri
 */
public class SecretValueView {
    public final String encoding;
    public final String type;
    public final String secretValue;

    SecretValueView(SecretValue secretValue) {
        this.encoding = secretValue.encoding.toString();
        this.type = secretValue.type.toString();

        if (secretValue.encoding == Encoding.UTF8) {
            this.secretValue = secretValue.asString();
        } else {
            this.secretValue = Encoder.base64encode(secretValue.asByteArray());
        }
    }
}
