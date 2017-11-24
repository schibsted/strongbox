/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.gui.view;

import com.schibsted.security.strongbox.gui.component.KeyValueHBox;
import com.schibsted.security.strongbox.gui.modals.UpdateSecret;
import com.schibsted.security.strongbox.sdk.SecretsGroup;
import com.schibsted.security.strongbox.sdk.internal.converter.FormattedTimestamp;
import com.schibsted.security.strongbox.sdk.types.Comment;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretMetadata;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 * @author stiankri
 */
public class SecretEntryView extends StackPane {
    VBox currentSecretView = new VBox();
    SecretsGroup secretsGroup;
    RawSecretEntry rawSecretEntry;
    SecretIdentifier secretIdentifier;
    long secretVersion;

    Button edit;
    Button downloadSecret;
    Stage stage;
    HBox editRow;

    public SecretEntryView(SecretsGroup secretsGroup, RawSecretEntry rawSecretEntry, SecretIdentifier secretIdentifier, long secretVersion, Stage stage) {
        this.rawSecretEntry = rawSecretEntry;
        this.secretsGroup = secretsGroup;
        this.secretIdentifier = secretIdentifier;
        this.secretVersion = secretVersion;
        this.stage = stage;

        edit = new Button("Edit");
        edit.setOnAction(e -> {
            UpdateSecret updateSecret = new UpdateSecret(stage);
            Optional<SecretMetadata> secretMetadata = updateSecret.getUpdateSecret(this.secretsGroup, this.rawSecretEntry, this.secretIdentifier, this.secretVersion);

            if (secretMetadata.isPresent()) {
                this.rawSecretEntry = secretsGroup.update(secretMetadata.get());
                outputNonEncrypted();
            }
        });

        downloadSecret = new Button("Download Secret");
        downloadSecret.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Download Secret as File");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

            SecretEntry decryptedEntry = secretsGroup.decryptEvenIfNotActive(rawSecretEntry, secretIdentifier, secretVersion);
            fileChooser.setInitialFileName(decryptedEntry.secretIdentifier.name);
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    Files.write(file.toPath(), decryptedEntry.secretValue.asByteArray());
                    decryptedEntry.bestEffortShred();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write secret to file", e);
                }
            }
        });

        editRow = new HBox();
        editRow.getChildren().setAll(edit, downloadSecret);

        setPrefWidth(380);
        outputNonEncrypted();
        getChildren().setAll(currentSecretView);
    }

    private void outputNonEncrypted() {
        Button viewEncryptedData;
        viewEncryptedData =  new Button("View Encrypted Data");
        viewEncryptedData.setOnAction(e -> {
            outputDecrypted();
        });

        outputShared();
        currentSecretView.getChildren().addAll(viewEncryptedData);
    }

    private void outputShared() {
        HBox name = KeyValueHBox.createTableRow("Name:", rawSecretEntry.secretIdentifier.name);
        HBox version = KeyValueHBox.createTableRow("Version:", rawSecretEntry.version.toString());

        Label filler = new Label(" ");
        filler.setMinHeight(27);

        HBox state = KeyValueHBox.createTableRow("State:", rawSecretEntry.state.toString());
        HBox notBefore = KeyValueHBox.createTableRow("Not Before:", FormattedTimestamp.toHumanReadable(rawSecretEntry.notBefore));
        HBox notAfter = KeyValueHBox.createTableRow("Not After:", FormattedTimestamp.toHumanReadable(rawSecretEntry.notAfter));

        Label title = new Label("Entry");

        currentSecretView.getChildren().setAll(title, editRow, filler, name, version, state, notBefore, notAfter);
    }

    private void outputDecrypted() {
        SecretEntry secretEntry = secretsGroup.decryptEvenIfNotActive(rawSecretEntry, secretIdentifier, secretVersion);

        String secret = secretEntry.secretValue.asString();
        HBox secretValue = KeyValueHBox.createTextAreaRow("Value:", secret);
        HBox created = KeyValueHBox.createTableRow("Created:", FormattedTimestamp.toHumanReadable(secretEntry.created));
        HBox modified = KeyValueHBox.createTableRow("Modified:", FormattedTimestamp.toHumanReadable(secretEntry.modified));
        HBox comment = KeyValueHBox.createTableRow("Comment:", secretEntry.comment.map(Comment::asString));

        Button hideEncryptedData;
        hideEncryptedData =  new Button("Hide Encrypted Data");
        hideEncryptedData.setOnAction(e -> {
            outputNonEncrypted();
        });

        secretEntry.bestEffortShred();

        outputShared();
        currentSecretView.getChildren().addAll(created, modified, comment, secretValue, hideEncryptedData);
    }
}
