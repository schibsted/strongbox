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

package com.schibsted.security.strongbox.gui.view;

import com.schibsted.security.strongbox.gui.modals.GetNewSecret;
import com.schibsted.security.strongbox.sdk.SecretsGroup;
import com.schibsted.security.strongbox.sdk.internal.srn.SecretSRN;
import com.schibsted.security.strongbox.sdk.types.NewSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SRN;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author stiankri
 */
public class SecretsView extends Pane {
    HBox currentBox = new HBox();

    VBox secretBox = new VBox();
    ListView<String> secretList = new ListView<>();

    private VersionView versionView;

    SecretIdentifier currentSecret;

    SecretsGroup secretsGroup;

    HBox secretRow;
    HBox perElementSecretRow;
    Label secretsLabel;
    Label filler = new Label(" ");
    Stage stage;

    public SecretsView(SecretsGroup secretsGroup, Stage stage) {
        this.secretsGroup = secretsGroup;
        this.stage = stage;

        secretRow = new HBox();
        perElementSecretRow = new HBox();
        Button createNewSecret =  new Button("New");
        Button refreshSecret =  new Button("Refresh");
        Button deleteSecret =  new Button("Delete");

        secretsLabel = new Label("Secrets");

        filler.setMinHeight(27);

        secretRow.getChildren().addAll(createNewSecret, refreshSecret);
        perElementSecretRow.getChildren().setAll(deleteSecret);

        loadSecrets();

        getChildren().setAll(currentBox);

        secretList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            secretBox.getChildren().setAll(secretsLabel, secretRow, perElementSecretRow, secretList);

            currentSecret = new SecretIdentifier(newValue);
            versionView = new VersionView(secretsGroup, currentSecret, stage);

            currentBox.getChildren().setAll(secretBox, versionView);
        });

        refreshSecret.setOnAction(e -> {
            loadSecrets();
        });

        createNewSecret.setOnAction(e -> {
            GetNewSecret getNewSecret = new GetNewSecret(stage, "Create New Secret");
            Optional<NewSecretEntry> newSecretEntry = getNewSecret.getNewSecret();

            if (newSecretEntry.isPresent()) {
                secretsGroup.create(newSecretEntry.get());
                loadSecrets();
                newSecretEntry.get().bestEffortShred();
            }
        });

        deleteSecret.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            SecretSRN srn = (SecretSRN) secretsGroup.srn(currentSecret);
            alert.setHeaderText(String.format("Are you sure you want to delete the secret '%s' in the group '%s' in '%s'.", currentSecret.name, srn.groupIdentifier.name, srn.groupIdentifier.region.name));
            alert.setContentText(String.format("Full SRN: %s\n\nThis will permanently delete the secret unless you first make a backup. Please see the documentation for more information.\n\nAre you sure you want to delete the secret?", srn));

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                secretsGroup.delete(currentSecret);
                loadSecrets();
            }
        });
    }

    private void loadSecrets() {
        secretList.setItems(FXCollections.emptyObservableList());
        secretList.setItems(getSecretNames(secretsGroup));
        secretBox.getChildren().setAll(secretsLabel, secretRow, filler, secretList);
        currentBox.getChildren().setAll(secretBox);
    }

    ObservableList<String> getSecretNames(SecretsGroup sg) {
        List<String> secretNames = sg.identifiers().stream().map(i -> i.name).sorted().collect(Collectors.toList());
        return FXCollections.observableArrayList(secretNames);
    }
}
