/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.gui.modals;

import com.amazonaws.util.Base64;
import com.schibsted.security.strongbox.gui.Singleton;
import com.schibsted.security.strongbox.sdk.internal.converter.SecretValueConverter;
import com.schibsted.security.strongbox.sdk.types.Comment;
import com.schibsted.security.strongbox.sdk.types.NewSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretType;
import com.schibsted.security.strongbox.sdk.types.SecretValue;
import com.schibsted.security.strongbox.sdk.types.State;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * @author stiankri
 */
public class GetNewSecret {
    private String title;
    private Optional<NewSecretEntry> newSecretEntry = Optional.empty();
    private Optional<SecretIdentifier> secretIdentifier = Optional.empty();

    private final Stage parent;

    public GetNewSecret(Stage parent, String title) {
        this.parent = parent;
        this.title = title;
    }

    public GetNewSecret(Stage parent, String title, SecretIdentifier secretIdentifier) {
        this.parent = parent;
        this.title = title;
        this.secretIdentifier = Optional.of(secretIdentifier);
    }
    private File file;

    public Optional<NewSecretEntry> getNewSecret() {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parent);
        Label label = new Label(title);

        TextField secretName = new TextField();
        TextField secretValue = new TextField();
        TextField secretValueGenerated = new TextField();
        TextField comment = new TextField();

        GridPane layout = new GridPane();
        Text secretNameText = new Text("Name:");

        layout.add(label, 0, 0);

        layout.add(secretNameText, 0, 1);
        layout.add(secretName, 1, 1);

        if (secretIdentifier.isPresent()) {
            secretName.setText(secretIdentifier.get().name);
            secretName.setDisable(true);
        }

        ToggleGroup valueSource = new ToggleGroup();
        Label valueLabel = new Label("Value Type");
        RadioButton value = new RadioButton("value");
        value.setToggleGroup(valueSource);
        value.setUserData("value");
        value.setSelected(true);

        RadioButton generated = new RadioButton("generated");
        generated.setToggleGroup(valueSource);
        generated.setUserData("generated");


        RadioButton fromFile = new RadioButton("from file");
        fromFile.setToggleGroup(valueSource);
        fromFile.setUserData("fromFile");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        Button openFileChooser = new Button("Select file");

        HBox selectSource = new HBox();
        selectSource.getChildren().addAll(value, generated, fromFile);

        layout.add(valueLabel, 0, 2);
        layout.add(selectSource, 1, 2);

        Text secretValueText = new Text("Value:");

        HBox wrapperLabel = new HBox();
        HBox wrapperValue = new HBox();

        wrapperLabel.getChildren().setAll(secretValueText);
        wrapperValue.getChildren().setAll(secretValue);

        layout.add(wrapperLabel, 0, 3);
        layout.add(wrapperValue, 1, 3);


        valueSource.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.getUserData().equals("value")) {
                wrapperValue.getChildren().setAll(secretValue);
                secretValueText.setText("Value:");
            } else if (newValue.getUserData().equals("generated")) {
                wrapperValue.getChildren().setAll(secretValueGenerated);
                secretValueText.setText("#Bytes to generate:");
            } else {
                wrapperValue.getChildren().setAll(openFileChooser);
                secretValueText.setText("Value:");
            }
        });

        openFileChooser.setOnAction(a -> {
            file = fileChooser.showOpenDialog(dialog);
        });


        DatePicker notBefore = new DatePicker();
        Text notBeforeText = new Text("Not Before (optional):");
        layout.add(notBeforeText, 0, 5);
        layout.add(notBefore, 1, 5);

        DatePicker notAfter = new DatePicker();
        Text notAfterText = new Text("Not After (optional):");
        layout.add(notAfterText, 0, 6);
        layout.add(notAfter, 1, 6);

        ObservableList<String> states =
                FXCollections.observableArrayList(
                        "Enabled",
                        "Disabled"
                );
        Text stateText = new Text("State:");
        ComboBox<String> state = new ComboBox<>(states);
        state.setValue("Enabled");
        layout.add(stateText, 0, 4);
        layout.add(state, 1, 4);

        Text commentText = new Text("Comment (optional):");
        layout.add(commentText, 0, 7);
        layout.add(comment, 1, 7);

        HBox actions = new HBox();
        Button create = new Button("Create");
        Button cancel = new Button("Cancel");
        actions.getChildren().addAll(create, cancel);

        layout.add(actions, 1, 8);


        create.setOnAction(f -> {
            // FIXME: proper testing and error handling
            if (secretName.getText() != null && (secretValue.getText() != null || secretValueGenerated.getText() != null || file != null)) {
                Optional<ZonedDateTime> nbf = getDate(notBefore);
                Optional<ZonedDateTime> na = getDate(notAfter);
                Optional<Comment> c = comment.getText().length() > 0 ? Optional.of(new Comment(comment.getText())) : Optional.empty();
                SecretValue sv = getSecretValue(valueSource, secretValue.getText(), secretValueGenerated.getText(), file);

                newSecretEntry = Optional.of(new NewSecretEntry(new SecretIdentifier(secretName.getText()), sv, getState(state.getValue()), nbf, na, c));
                dialog.close();
            }
        });

        cancel.setOnAction(f -> {
            dialog.close();
        });

        Scene scene2 = new Scene(layout, 400, 250);
        dialog.setScene(scene2);

        dialog.showAndWait();

        return newSecretEntry;
    }

    private Optional<ZonedDateTime> getDate(DatePicker datePicker) {
        return datePicker.getValue() != null
                ? Optional.of(ZonedDateTime.of(datePicker.getValue().atTime(0, 0), ZoneId.of("UTC")))
                : Optional.empty();
    }

    private SecretValue getSecretValue(ToggleGroup valueSource, String value, String generated, File file) {
        Toggle current = valueSource.getSelectedToggle();

        String secretString;
        if (current.getUserData().equals("value")) {
            secretString = value;
        } else if (current.getUserData().equals("generated")) {
            Integer numBytesToGenerate = Integer.valueOf(generated);
            // TODO: store as plain bytes?
            byte[] random = Singleton.randomGenerator.generateRandom(numBytesToGenerate);
            secretString = Base64.encodeAsString(random);
        } else {
            String path = null;
            try {
                path = file.getCanonicalPath();
                return SecretValueConverter.inferEncoding(Files.readAllBytes(Paths.get(path)), SecretType.OPAQUE);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read secret from file");
            }
        }

        return new SecretValue(secretString, SecretType.OPAQUE);
    }

    private State getState(String state) {
        switch(state) {
            case "Enabled":
                return State.ENABLED;
            case "Disabled":
                return State.DISABLED;
            default:
                throw new RuntimeException("unrecognized state " + state);
        }
    }
}
