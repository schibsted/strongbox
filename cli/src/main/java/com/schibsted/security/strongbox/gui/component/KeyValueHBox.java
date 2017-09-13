/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Schibsted Products & Technology AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
