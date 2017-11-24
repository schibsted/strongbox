/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.gui.component;


import javafx.scene.control.TextArea;

/**
 * @author stiankri
 */
public class SelectableTextArea extends TextArea {

    public SelectableTextArea(String value, double width, int rowCount) {
        super(value);
        setPrefWidth(width);
        setPrefRowCount(rowCount);
        setEditable(false);
        setStyle("text-area-background-color: transparent;");
    }
}
