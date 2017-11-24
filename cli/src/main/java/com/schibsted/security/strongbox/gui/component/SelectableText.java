/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.gui.component;

import javafx.scene.control.TextField;

/**
 * @author stiankri
 */
public class SelectableText extends TextField {

    SelectableText(String value) {
        super(value);
        setEditable(false);
        setStyle("-fx-background-color: transparent; -fx-border-color: lightgrey;");
    }

    SelectableText(String value, double width) {
        this(value);
        setPrefWidth(width);
    }
}
