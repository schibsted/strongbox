/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.gui.modals;

import com.schibsted.security.strongbox.gui.Singleton;
import com.schibsted.security.strongbox.gui.component.KeyValueHBox;
import com.schibsted.security.strongbox.sdk.SecretsGroupManager;
import com.schibsted.security.strongbox.sdk.types.Principal;
import com.schibsted.security.strongbox.sdk.types.PrincipalType;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author stiankri
 */
public class GetUpdatePolicies {
    Optional<Principal> currentlySelectedAdmin = Optional.empty();
    Optional<Principal> currentlySelectedReadonly = Optional.empty();

    ObservableList<Principal> readonly;
    ObservableList<Principal> admins;

    ContextMenu suggestions;

    TextField addPrincipal;

    SecretsGroupIdentifier secretsGroupIdentifier;
    SecretsGroupManager secretsGroupManager;

    private final Stage parent;

    public GetUpdatePolicies(Stage parent, SecretsGroupManager secretsGroupManager, SecretsGroupIdentifier secretsGroupIdentifier) {
        this.parent = parent;
        this.secretsGroupManager = secretsGroupManager;
        this.secretsGroupIdentifier = secretsGroupIdentifier;
    }

    CustomMenuItem getSuggestion(String suggestion) {
        Label entryLabel = new Label(suggestion);
        CustomMenuItem item = new CustomMenuItem(entryLabel, true);
        item.setOnAction(f -> {
            addPrincipal.setText(suggestion);
        });
        return item;
    }

    public void updatePolicies() {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parent);

        VBox layout = new VBox();
        Label label = new Label("Update Policies");

        HBox current = KeyValueHBox.create("Secrets Group:", secretsGroupIdentifier.toString());

        SecretsGroupInfo info = secretsGroupManager.info(secretsGroupIdentifier);
        admins = FXCollections.observableArrayList(info.admin);
        ListView<Principal> adminsList = new ListView<>();
        Label adminListLabel= new Label("Admins");
        adminsList.setItems(admins);
        HBox adminRow = new HBox();
        Button deleteAdmin = new Button("Delete Selected Admin");
        adminRow.getChildren().addAll(adminListLabel, deleteAdmin);

        readonly = FXCollections.observableArrayList(info.readOnly);
        ListView<Principal> readonlyList = new ListView<>();
        Label readonlyLabel= new Label("ReadOnly");
        readonlyList.setItems(readonly);
        HBox readonlyRow = new HBox();
        Button deleteReadonly = new Button("Delete Selected ReadOnly");
        readonlyRow.getChildren().addAll(readonlyLabel, deleteReadonly);

        HBox addAdminRow = new HBox();
        Label addAdminLabel = new Label("Name: ");
        addPrincipal = new TextField();
        Button addAdminButton = new Button("Add admin");
        Button addReadOnlyButton = new Button("Add readonly");
        addAdminRow.getChildren().addAll(addAdminLabel, addPrincipal, addReadOnlyButton, addAdminButton);

        suggestions = new ContextMenu();

        Button close = new Button("Done");

        layout.getChildren().addAll(label, current, addAdminRow, readonlyRow, readonlyList, adminRow, adminsList, close);

        close.setOnAction(e -> {
            dialog.close();
        });

        addPrincipal.setOnKeyReleased(e -> {
            List<CustomMenuItem> l = new ArrayList<>();

            // get suggestions, add each one
            List<Principal> matches = Singleton.principalAutoSuggestion.autoSuggestion(addPrincipal.getText());
            matches.forEach(p -> l.add(getSuggestion(p.name)));

            suggestions.getItems().setAll(l);
            suggestions.show(addPrincipal, Side.BOTTOM, 0, 0);
        });

        addAdminButton.setOnAction(e -> {
            if (addPrincipal.getText() != null) {
                Principal principal = new Principal(PrincipalType.ROLE, addPrincipal.getText());

                if (!admins.stream().anyMatch(c -> c.equals(principal))) {
                    secretsGroupManager.attachAdmin(secretsGroupIdentifier, principal);
                    admins.addAll(principal);
                }
            }
        });

        addReadOnlyButton.setOnAction(e -> {
            if (addPrincipal.getText() != null) {
                Principal principal = new Principal(PrincipalType.ROLE, addPrincipal.getText());
                if (!readonly.stream().anyMatch(c -> c.equals(principal))) {
                    secretsGroupManager.attachReadOnly(secretsGroupIdentifier, principal);
                    readonly.addAll(principal);
                }
            }
        });

        adminsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            currentlySelectedAdmin = Optional.of(newValue);
        });

        readonlyList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            currentlySelectedReadonly = Optional.of(newValue);
        });

        deleteAdmin.setOnAction(e -> {
            if (currentlySelectedAdmin.isPresent()) {
                secretsGroupManager.detachAdmin(secretsGroupIdentifier, currentlySelectedAdmin.get());
                List<Principal> list = readonly.stream().filter(p -> !p.equals(currentlySelectedAdmin.get())).collect(Collectors.toList());
                admins = FXCollections.observableArrayList(list);
                adminsList.setItems(admins);
            }
        });

        deleteReadonly.setOnAction(e -> {
            if (currentlySelectedReadonly.isPresent()) {
                secretsGroupManager.detachReadOnly(secretsGroupIdentifier, currentlySelectedReadonly.get());
                List<Principal> list = readonly.stream().filter(p -> !p.equals(currentlySelectedReadonly.get())).collect(Collectors.toList());
                readonly = FXCollections.observableArrayList(list);
                readonlyList.setItems(readonly);
            }
        });

        Scene scene2 = new Scene(layout, 450, 250);
        dialog.setScene(scene2);

        dialog.showAndWait();
    }
}
