/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.view;

/**
 * @author stiankri
 */
public enum OutputFormat {
    TEXT, JSON, RAW, CSV;

    public boolean isCSVorRAW() {
        return this == OutputFormat.CSV || this == OutputFormat.RAW;
    }
}
