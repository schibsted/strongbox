/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.gui.component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author stiankri
 */
public class EnumComboBox {
    public static <T extends Enum<T>> ComboBox<T> create(Class<T> enumType, T defaultEnumValue) {

        List<T> entries = new ArrayList<>();
        Collections.addAll(entries, enumType.getEnumConstants());

        ObservableList<T> observableList = FXCollections.observableList(entries);
        ComboBox<T> comboBox = new ComboBox<T>(observableList);
        comboBox.setValue(defaultEnumValue);

        return comboBox;
    }
}
