/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.view;

import com.schibsted.security.strongbox.cli.StrongboxCLI;
import com.schibsted.security.strongbox.cli.viewmodel.SecretModel;
import io.airlift.airline.Command;
import io.airlift.airline.Help;
import io.airlift.airline.Option;

import java.util.Collections;

/**
 * CLI commands related to managing Secrets within a Strongbox Secrets Group
 *
 * @author stiankri
 * @author hawkaa
 */
public class Secret {

    @Command(name = "secret-help", description = "Display help information for 'secret'")
    public static class SecretHelp implements Runnable {
        @Override
        public void run() {
            Help.help(StrongboxCLI.globalMetadata, Collections.singletonList("secret"));
        }
    }

    public static class SecretsGroupBaseCommand extends Global.BaseCommand {
        @Option(name = "--group", description = "Group", required=true)
        public String groupName;

        protected SecretModel secretModel() {
            return groupModel().get(groupName);
        }
    }

    public static class SecretBaseCommand extends SecretsGroupBaseCommand {
        @Option(name = "--name", description = "Secret Name", required=true)
        public String secretName;
    }

    public static class ModifySecretBaseCommand extends SecretBaseCommand {
        @Option(name = "--state", description = "State {enabled, disabled}", required=false)
        public String stateName;

        @Option(name = "--comment", description = "Comment", required=false)
        public String comment;

        @Option(name = "--value-from-stdin", description = "Either pipe in or confidentially type/paste secret to avoid the secret ending up in your history", required=false)
        public boolean valueFromStdin;

        @Option(name = "--generate-value", description = "Generate cryptographically secure secret value", required=false)
        public String numBytes;

        @Option(name = "--value-from-file", description = "Import secret value from file", required=false)
        public String path;

        @Option(name = "--not-before", description = "Disabled Before (YYYY-MM-DD)", required=false)
        public String notBefore;

        @Option(name = "--not-after", description = "Disabled After (YYYY-MM-DD)", required=false)
        public String notAfter;
    }

    @Command(name = "create", description = "Create Secret")
    public static class Create extends ModifySecretBaseCommand
    {
        @Override
        public void run() {
            try (SecretModel sm = secretModel()) {
                sm.createSecret(secretName, valueFromStdin, stateName, notBefore, notAfter, comment, numBytes, path);
            }
        }
    }

    @Command(name = "add-version", description = "Add a New Version of the Secret")
    public static class AddVersion extends ModifySecretBaseCommand
    {
        @Override
        public void run() {
            try (SecretModel sm = secretModel()) {
                sm.addSecretVersion(secretName, valueFromStdin, stateName, notBefore, notAfter, comment, numBytes, path);
            }
        }
    }

    @Command(name = "list-versions", description = "List Versions of Secret")
    public static class ListVersions extends SecretBaseCommand
    {
        @Option(name = "--decrypt", description = "Decrypt entries to see additional metadata", required=false)
        public boolean decrypt;

        @Override
        public void run() {
            try (SecretModel sm = secretModel()) {
                renderer().render(sm.listSecretVersions(secretName, decrypt));
            }
        }
    }

    @Command(name = "list", description = "List Secrets")
    public static class ListNames extends SecretsGroupBaseCommand
    {
        @Override
        public void run() {
            try (SecretModel sm = secretModel()) {
                renderer().render(sm.getIdentifiers());
            }
        }
    }

    @Command(name = "get-active", description = "Get Active Secret Entry of Secret(s)")
    public static class GetActive extends SecretsGroupBaseCommand
    {
        @Option(name = "--name", description = "Secret Name", required=false)
        public String secretName;

        @Option(name = "--version", description = "Version", required=false)
        public String version;

        @Option(name = "--all", arity = 0, description = "Get All Active Versions", required=false)
        public boolean allEnabled;

        @Override
        public void run() {
            try (SecretModel sm = secretModel()) {
                renderer().render(sm.getActive(secretName, version, allEnabled));
            }
        }
    }

    @Command(name = "get-latest-active", description = "Get the latest Active Secret Entry of Secret(s)")
    public static class GetLatest extends SecretsGroupBaseCommand
    {
        @Option(name = "--name", description = "Secret Name", required=false)
        public String secretName;

        @Option(name = "--all", arity = 0, description = "Get Latest Active Version of All Secrets", required=false)
        public boolean all;

        @Override
        public void run() {
            try (SecretModel sm = secretModel()) {
                renderer().render(sm.getLatestActive(secretName, all));
            }
        }
    }

    @Command(name = "get", description = "Get the Secret Value of an Active Secret")
    public static class Get extends SecretsGroupBaseCommand
    {
        @Option(name = "--name", description = "Secret Name", required=true)
        public String secretName;

        @Option(name = "--version", description = "Version", required=false)
        public String version;

        @Override
        public void run() {
            try (SecretModel sm = secretModel()) {
                // Fetching the value of a specific secret is a very common goal, which is why we want to override
                // TEXT with RAW for this command
                Renderer renderer = (groupModel().getOutputFormat() == OutputFormat.TEXT)
                        ? new Renderer(OutputFormat.RAW, "secretValue.secretValue", groupModel().getSaveToFilePath())
                        : renderer();

                renderer.render(sm.get(secretName, version));
            }
        }
    }


    @Command(name = "delete", description = "Delete Secret")
    public static class Delete extends SecretBaseCommand
    {
        @Option(name = "--force", description = "Do not ask for confirmation", required = false)
        Boolean force;

        @Override
        public void run() {
            try (SecretModel sm = secretModel()) {
                sm.deleteSecret(secretName, force);
            }
        }
    }

    @Command(name = "update", description = "Update Metadata of Secret")
    public static class Update extends SecretBaseCommand
    {
        @Option(name = "--version", description = "Version", required=true)
        public String version;

        @Option(name = "--state", description = "State {enabled, disabled, compromised}", required=false)
        public String stateName;

        @Option(name = "--comment", description = "Comment", required=false)
        public String comment;

        @Override
        public void run() {
            try (SecretModel sm = secretModel()) {
                sm.setMetadata(secretName, version, stateName, comment);
            }
        }
    }
}
