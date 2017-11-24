/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.gui.view;

import com.schibsted.security.strongbox.gui.modals.GetGroupIdentifier;
import com.schibsted.security.strongbox.gui.modals.GetUpdatePolicies;
import com.schibsted.security.strongbox.sdk.impl.DefaultSecretsGroupManager;
import com.schibsted.security.strongbox.sdk.internal.encryption.FileEncryptionContext;
import com.schibsted.security.strongbox.sdk.internal.impl.DefaultSecretsGroup;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * @author stiankri
 */
public class GroupsView extends Pane {

    DefaultSecretsGroupManager secretsGroupManager;
    Region region;

    Label secretGroupLabel;

    VBox secretsGroupBox;

    HBox currentBox = new HBox();

    HBox secretsGroupRow;
    HBox perElementSecretsGroupRow;

    Button createNewSecretsGroup;

    Button deleteSecretsGroup;

    Button refreshGroup;

    Button policies;
    Button backup;
    Button restore;

    Label filler2;

    ListView<SecretsGroupIdentifier> groupList;

    SecretsGroupIdentifier currentGroup;
    DefaultSecretsGroup currentSecretsGroup;

    SecretsView secretsView;
    Stage stage;

    private static final String FILE_ENDING = "sbx";

    public GroupsView(Stage stage, DefaultSecretsGroupManager secretsGroupManager, Region region) {
        this.secretsGroupManager = secretsGroupManager;
        this.region = region;
        this.stage = stage;

        groupList = new ListView<>();

        secretsGroupBox = new VBox();

        secretsGroupRow = new HBox();
        perElementSecretsGroupRow = new HBox();
        secretGroupLabel = new Label("Secrets Groups");

        createNewSecretsGroup =  new Button("New");

        deleteSecretsGroup =  new Button("Delete");

        refreshGroup =  new Button("Refresh");

        policies =  new Button("Policies");
        backup = new Button("Backup");
        restore = new Button("Restore");

        filler2 = new Label(" ");
        filler2.setMinHeight(27);

        secretsGroupRow.getChildren().addAll(createNewSecretsGroup, refreshGroup);
        perElementSecretsGroupRow.getChildren().addAll(policies, deleteSecretsGroup, backup, restore);

        groupList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            secretsGroupBox.getChildren().setAll(secretGroupLabel, secretsGroupRow, perElementSecretsGroupRow, groupList);

            currentGroup = newValue;
            currentSecretsGroup = (DefaultSecretsGroup)secretsGroupManager.get(currentGroup);

            loadSecrets();
        });

        createNewSecretsGroup.setOnAction(e -> {
            GetGroupIdentifier getGroupIdentifier = new GetGroupIdentifier(stage, region);
            Optional<SecretsGroupIdentifier> result = getGroupIdentifier.getGroupIdentifier();

            if (result.isPresent()) {
                secretsGroupManager.create(result.get());
                loadGroupNames();
            }
        });

        policies.setOnAction(e -> {
            GetUpdatePolicies getUpdatePolicies = new GetUpdatePolicies(stage, secretsGroupManager, currentGroup);
            getUpdatePolicies.updatePolicies();
        });


        refreshGroup.setOnAction(e -> {
            loadGroupNames();
        });

        deleteSecretsGroup.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText(String.format("Are you sure you want to delete the group '%s' in the region '%s'?", currentGroup.name, currentGroup.region.name));
            alert.setContentText(String.format("Full SRN: %s\n\nYou might not be able to recover from this action. Please see the documentation for more information.\n\nAre you sure you want to delete the group?", secretsGroupManager.srn(currentGroup)));

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                secretsGroupManager.delete(currentGroup);
                loadGroupNames();
            }
        });

        backup.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Backup Secrets Group To File");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            fileChooser.setInitialFileName(String.format("%s.%s.%s", currentGroup.name, currentGroup.region.getName(), FILE_ENDING));
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                com.schibsted.security.strongbox.sdk.internal.kv4j.generated.File fileStore = new com.schibsted.security.strongbox.sdk.internal.kv4j.generated.File(file,
                        ((DefaultSecretsGroupManager)secretsGroupManager).encryptor(currentGroup),
                        new FileEncryptionContext(currentGroup),
                        new ReentrantReadWriteLock());
                ((DefaultSecretsGroupManager) secretsGroupManager).backup(currentGroup, fileStore, false);
            }
        });

        restore.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText(String.format("Are you sure you want to restore the group '%s' in the region '%s'?", currentGroup.name, currentGroup.region.name));
            alert.setContentText(String.format("Full SRN: %s\n\nThis action is potentially risky: it will permanently delete all secrets you currently have in the group before replacing them with the secrets in the file you will choose in the next step.", secretsGroupManager.srn(currentGroup)));

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Restore Secrets Group From File");
                FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Strongbox Files", "*." + FILE_ENDING);
                fileChooser.getExtensionFilters().add(extensionFilter);
                fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                File file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    com.schibsted.security.strongbox.sdk.internal.kv4j.generated.File fileStore = new com.schibsted.security.strongbox.sdk.internal.kv4j.generated.File(file,
                            secretsGroupManager.encryptor(currentGroup),
                            new FileEncryptionContext(currentGroup),
                            new ReentrantReadWriteLock());
                    secretsGroupManager.restore(currentGroup, fileStore, false);
                    loadSecrets();
                }
            }
        });

        loadGroupNames();
        getChildren().setAll(currentBox);
    }

    private void loadGroupNames() {
        groupList.setItems(FXCollections.emptyObservableList());
        groupList.setItems(getGroupNames());
        secretsGroupBox.getChildren().setAll(secretGroupLabel, secretsGroupRow, filler2, groupList);
        currentBox.getChildren().setAll(secretsGroupBox);
    }

    private void loadSecrets() {
        secretsView = new SecretsView(currentSecretsGroup, stage);
        currentBox.getChildren().setAll(secretsGroupBox, secretsView);
    }

    ObservableList<SecretsGroupIdentifier> getGroupNames() {
        List<SecretsGroupIdentifier> groupNames = secretsGroupManager.identifiers().stream().sorted().collect(Collectors.toList());
        return FXCollections.observableArrayList(groupNames);
    }
}
