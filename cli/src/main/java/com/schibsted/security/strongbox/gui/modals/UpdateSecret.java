/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.gui.modals;

import com.schibsted.security.strongbox.gui.component.EnumComboBox;
import com.schibsted.security.strongbox.gui.component.KeyValueHBox;
import com.schibsted.security.strongbox.sdk.SecretsGroup;
import com.schibsted.security.strongbox.sdk.types.Comment;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretMetadata;
import com.schibsted.security.strongbox.sdk.types.State;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;

/**
 * @author stiankri
 */
public class UpdateSecret {
    private final Stage parent;
    Optional<SecretMetadata> metadata = Optional.empty();

    public UpdateSecret(Stage parent) {
        this.parent = parent;
    }

    public Optional<SecretMetadata> getUpdateSecret(SecretsGroup secretsGroup, RawSecretEntry rawSecretEntry, SecretIdentifier secretIdentifier, long secretVersion) {

        SecretEntry secretEntry = secretsGroup.decryptEvenIfNotActive(rawSecretEntry, secretIdentifier, secretVersion);
        Optional<String> comment = secretEntry.comment.map(Comment::asString);
        State state = secretEntry.state;
        secretEntry.bestEffortShred();

        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parent);
        Label label = new Label("Update Secret");

        VBox layout = new VBox();

        HBox nameLine = KeyValueHBox.create("Name: ", secretIdentifier.name);
        HBox versionLine = KeyValueHBox.create("Version: ", secretVersion);

        ComboBox<State> statesComboBox = EnumComboBox.create(State.class, state);

        HBox stateLine = KeyValueHBox.create("State: ", statesComboBox);

        TextField commentValue = new TextField();
        commentValue.setText(comment.orElse(""));

        HBox commentLine = KeyValueHBox.create("Comment: ", commentValue);

        HBox actions = new HBox();
        Button create = new Button("Update");
        Button cancel = new Button("Cancel");
        actions.getChildren().addAll(create, cancel);

        layout.getChildren().setAll(label, nameLine, versionLine, stateLine, commentLine, actions);

        create.setOnAction(e -> {
            Optional<Comment> newComment = commentValue.getText().equals("") ? Optional.empty() : Optional.of(new Comment(commentValue.getText()));

            Optional<State> newState = Optional.of(statesComboBox.getValue());

            metadata = Optional.of(new SecretMetadata(secretIdentifier,
                    secretVersion,
                    newState,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(newComment)));

            dialog.close();
        });

        cancel.setOnAction(e -> {
            dialog.close();
        });

        Scene scene = new Scene(layout, 400, 250);
        dialog.setScene(scene);

        dialog.showAndWait();

        return metadata;
    }


}
