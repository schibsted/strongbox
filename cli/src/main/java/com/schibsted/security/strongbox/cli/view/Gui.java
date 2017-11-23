/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.view;

import com.schibsted.security.strongbox.cli.viewmodel.GroupModel;
import com.schibsted.security.strongbox.gui.StrongboxGUI;
import com.schibsted.security.strongbox.gui.Singleton;
import io.airlift.airline.Command;

/**
 * Bootstrap and launch the GUI via the CLI
 *
 * @author stiankri
 */
public class Gui {
    @Command(name = "gui", description = "Open Graphical User Interface")
    public static class OpenGui extends Global.BaseCommand {
        @Override
        public void run() {
            try {
                GroupModel groupModel = groupModel();
                Singleton.secretsGroupManager = groupModel.getSecretsGroupManager();
                Singleton.region = groupModel.getRegion();
                Singleton.randomGenerator = groupModel.getRandomGenerator();
                Singleton.principalAutoSuggestion = groupModel.getPrincipalAutoSuggestion();
                StrongboxGUI strongboxGUI = new StrongboxGUI();
                strongboxGUI.run();
            } catch (java.lang.NoClassDefFoundError e) {
                if (e.getMessage().equals("javafx/application/Application")) {
                    throw new RuntimeException("Unable to launch GUI since JavaFX is not installed.", e);
                } else {
                    throw e;
                }
            }
        }
    }
}
