/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.gui.view;

import com.schibsted.security.strongbox.gui.modals.GetNewSecret;
import com.schibsted.security.strongbox.sdk.SecretsGroup;
import com.schibsted.security.strongbox.sdk.types.NewSecretEntry;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.name;
import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.version;

/**
 * @author stiankri
 */
public class VersionView extends Pane {
    HBox currentBox = new HBox();
    VBox versionBox = new VBox();
    ListView<Long> versionList = new ListView<>();
    long currentVersion;

    private final SecretsGroup secretsGroup;
    private final SecretIdentifier secretIdentifier;
    private RawSecretEntry rawSecretEntry;

    private SecretEntryView entry;

    private Stage stage;

    public VersionView(SecretsGroup secretsGroup, SecretIdentifier secretIdentifier, Stage stage) {
        this.secretsGroup = secretsGroup;
        this.secretIdentifier = secretIdentifier;
        this.stage = stage;

        versionList.setItems(getSecretVersions(secretsGroup, secretIdentifier));

        Label versionLabel = new Label("Versions");
        HBox versionRow = new HBox();

        Button createNewVersion =  new Button("New");
        Button refreshVersion =  new Button("Refresh");

        versionRow.getChildren().addAll(createNewVersion, refreshVersion);

        Label filler;
        filler = new Label(" ");
        filler.setMinHeight(27);

        versionBox.getChildren().setAll(versionLabel, versionRow, filler, versionList);

        refreshVersion.setOnAction(e -> {
            loadVersions();
        });

        versionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            currentVersion = newValue;
            rawSecretEntry = getRawSecretEntry(secretsGroup, secretIdentifier, currentVersion);

            entry = new SecretEntryView(secretsGroup, rawSecretEntry, secretIdentifier, currentVersion, stage);

            currentBox.getChildren().setAll(versionBox, entry);
        });

        createNewVersion.setOnAction(e -> {
            GetNewSecret getNewSecret = new GetNewSecret(stage, "Create New Secret Version", secretIdentifier);
            Optional<NewSecretEntry> newSecretEntry = getNewSecret.getNewSecret();

            if (newSecretEntry.isPresent()) {
                secretsGroup.addVersion(newSecretEntry.get());
                loadVersions();
                newSecretEntry.get().bestEffortShred();
            }
        });

        currentBox.getChildren().addAll(versionBox);

        getChildren().addAll(currentBox);
    }

    private void loadVersions() {
        versionList.setItems(getSecretVersions(secretsGroup, secretIdentifier));
    }

    RawSecretEntry getRawSecretEntry(SecretsGroup secretsGroup, SecretIdentifier secretIdentifier, long targetVersion) {
        return secretsGroup.stream()
                .filter(name.eq(secretIdentifier).AND(version.eq(targetVersion)))
                .findFirst().get();
    }

    ObservableList<Long> getSecretVersions(SecretsGroup sg, SecretIdentifier secret) {
        List<Long> secretVersions = sg.stream().filter(name.eq(secret)).toJavaStream().map(i -> i.version).collect(Collectors.toList());

        return FXCollections.observableArrayList(secretVersions);
    }
}
