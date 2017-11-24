/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.gui.component;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author stiankri
 */
public class KeyValueHBox {
    private static final double LABEL_COLUMN_WIDTH = 90;
    private static final double VALUE_COLUMN_WIDTH = 250;

    public static HBox createTableRow(String label, String value) {
        HBox hBox = new HBox();
        SelectableText l = new SelectableText(label, LABEL_COLUMN_WIDTH);
        SelectableText v = new SelectableText(value, VALUE_COLUMN_WIDTH);
        hBox.getChildren().addAll(l, v);
        return hBox;
    }

    public static HBox createTextAreaRow(String label, String value) {
        HBox hBox = new HBox();
        SelectableText l = new SelectableText(label, LABEL_COLUMN_WIDTH);

        int newLineCount = 0;
        Matcher m = Pattern.compile("\r\n|\r|\n").matcher(value);
        while(m.find()) {
            newLineCount++;
        }

        SelectableTextArea v = new SelectableTextArea(value, VALUE_COLUMN_WIDTH, Math.min(newLineCount, 6));

        hBox.getChildren().addAll(l, v);
        return hBox;
    }

    public static HBox create(String label, String value) {
        HBox hBox = new HBox();
        Label l = new Label(label);
        Label v = new Label(value);
        hBox.getChildren().addAll(l, v);
        return hBox;
    }

    public static HBox create(String label, Node node) {
        HBox hBox = new HBox();
        Label l = new Label(label);
        hBox.getChildren().addAll(l, node);
        return hBox;
    }

    public static HBox create(String label, long value) {
        return create(label, Long.toString(value));
    }


    public static HBox createTableRow(String label, Optional<?> value) {
        if (value.isPresent()) {
            return createTableRow(label, value.get().toString());
        } else {
            return createTableRow(label, "");
        }
    }
}
