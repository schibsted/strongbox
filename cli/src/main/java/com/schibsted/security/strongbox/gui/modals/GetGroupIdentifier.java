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

package com.schibsted.security.strongbox.gui.modals;

import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;

/**
 * @author stiankri
 */
public class GetGroupIdentifier {
    private Optional<SecretsGroupIdentifier> secretsGroupIdentifier = Optional.empty();
    private Region defaultRegion;

    private final Stage parent;

    public GetGroupIdentifier(Stage parent, Region defaultRegion) {
        this.parent = parent;
        this.defaultRegion = defaultRegion;
    }

    public Optional<SecretsGroupIdentifier> getGroupIdentifier() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parent);
        dialog.initStyle(StageStyle.UTILITY);

        VBox layout = new VBox();
        Label label = new Label("Create Secrets Group");

        Text nameLabel = new Text("Name:");
        TextField name = new TextField();
        HBox n = new HBox();
        n.getChildren().addAll(nameLabel, name);

        ObservableList<Region> regions =
                FXCollections.observableArrayList(Region.values());

        Text regionLabel = new Text("Region:");
        ComboBox<Region> region = new ComboBox<>(regions);
        region.setValue(defaultRegion);


        HBox r = new HBox();
        r.getChildren().addAll(regionLabel, region);

        HBox actions = new HBox();
        Button create = new Button("Create");
        Button cancel = new Button("Cancel");
        actions.getChildren().addAll(create, cancel);

        create.setOnAction(f -> {
            if (name.getText() != null) {
                secretsGroupIdentifier = Optional.of(new SecretsGroupIdentifier(region.getValue(), name.getText()));
            }
            dialog.close();
        });

        cancel.setOnAction(f -> {
            dialog.close();
        });

        layout.getChildren().addAll(label, n, r, actions);

        Scene scene2 = new Scene(layout, 370, 250);
        dialog.setScene(scene2);

        dialog.showAndWait();


        return secretsGroupIdentifier;
    }
}
