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
    @Command(name = "open", description = "Open Graphical User Interface")
    public static class OpenGui extends Global.BaseCommand {
        @Override
        public void run() {
            GroupModel groupModel = groupModel();
            Singleton.secretsGroupManager = groupModel.getSecretsGroupManager();
            Singleton.region = groupModel.getRegion();
            Singleton.randomGenerator = groupModel.getRandomGenerator();
            Singleton.principalAutoSuggestion = groupModel.getPrincipalAutoSuggestion();
            StrongboxGUI strongboxGUI = new StrongboxGUI();
            strongboxGUI.run();
        }
    }
}
