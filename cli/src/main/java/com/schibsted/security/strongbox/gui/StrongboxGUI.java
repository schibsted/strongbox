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
