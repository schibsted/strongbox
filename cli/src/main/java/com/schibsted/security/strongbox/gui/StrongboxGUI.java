/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.gui;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.schibsted.security.strongbox.gui.view.GroupsView;
import com.schibsted.security.strongbox.sdk.internal.access.PrincipalAutoSuggestion;
import com.schibsted.security.strongbox.sdk.impl.DefaultSecretsGroupManager;
import com.schibsted.security.strongbox.sdk.internal.encryption.KMSRandomGenerator;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.schibsted.security.strongbox.sdk.types.Region;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * @author stiankri
 */
public class StrongboxGUI extends Application {
    HBox borderPane = new HBox();

    @Override
    public void start(Stage stage) throws Exception {
        GroupsView groupsView = new GroupsView(stage, (DefaultSecretsGroupManager) Singleton.secretsGroupManager, Singleton.region);

        Scene scene  = new Scene(borderPane,1100,500);
        scene.getStylesheets().add("style.css");
        stage.setScene(scene);

        borderPane.getChildren().addAll(groupsView);

        stage.show();
    }

    public void run() {
        launch();
    }

    public static void main(String[] args) {
        Singleton.secretsGroupManager = new DefaultSecretsGroupManager();
        Singleton.region = Region.EU_WEST_1;
        Singleton.randomGenerator = new KMSRandomGenerator();
        Singleton.principalAutoSuggestion = PrincipalAutoSuggestion.fromCredentials(new DefaultAWSCredentialsProviderChain(), new ClientConfiguration());
        StrongboxGUI strongboxGUI = new StrongboxGUI();
        strongboxGUI.run();
    }
}
