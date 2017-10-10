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

package com.schibsted.security.strongbox.cli;

import com.google.common.collect.ListMultimap;
import com.schibsted.security.strongbox.cli.view.Global;
import com.schibsted.security.strongbox.cli.view.Group;
import com.schibsted.security.strongbox.cli.view.Gui;
import com.schibsted.security.strongbox.cli.view.Secret;
import io.airlift.airline.Cli;
import io.airlift.airline.Cli.CliBuilder;
import io.airlift.airline.Help;
import io.airlift.airline.OptionType;
import io.airlift.airline.ParseArgumentsUnexpectedException;
import io.airlift.airline.Parser;
import io.airlift.airline.model.GlobalMetadata;
import io.airlift.airline.model.OptionMetadata;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Entry point for the CLI
 *
 * @author stiankri
 */
public class StrongboxCLI {
    public static GlobalMetadata globalMetadata;

    public static void main(String[] args) {
        CliBuilder<Runnable> builder = Cli.<Runnable>builder("strongbox")
                .withDescription("Strongbox")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class);

        builder.withCommand(Global.CustomHelp.class);
        builder.withCommand(Global.Version.class);
        builder.withCommand(Global.VersionOption.class);

        builder.withGroup("group")
                .withDescription("Manage Secret Groups")
                .withDefaultCommand(Group.GroupHelp.class)
                .withCommands(Group.Create.class, Group.List.class, Group.Info.class, Group.Delete.class, Group.AttachAdmin.class, Group.DetachAdmin.class, Group.AttachReadOnly.class, Group.DetachReadOnly.class, Group.BackupCommand.class, Group.RestoreCommand.class, Group.MigrateCommand.class);

        builder.withGroup("secret")
                .withDescription("Manage Secrets for a Secret Group")
                .withDefaultCommand(Secret.SecretHelp.class)
                .withCommands(Secret.Create.class, Secret.AddVersion.class, Secret.Get.class, Secret.GetLatest.class, Secret.Delete.class, Secret.ListNames.class, Secret.ListVersions.class, Secret.Update.class);

        builder.withCommand(Gui.OpenGui.class);

        Cli<Runnable> parser = builder.build();
        globalMetadata = parser.getMetadata();

        try {
            parser.parse(args).run();
        } catch (ParseArgumentsUnexpectedException exception) {
            Optional<String> globalOptions = exception.getUnparsedInput().stream()
                    .filter(Global.Option::contains)
                    .findAny();

            System.err.println(exception.getMessage());
            if (globalOptions.isPresent()) {
                System.err.println(String.format(
                        "Please note: global options like '%s' must be placed before the command,\n" +
                        "  e.g. 'strongbox --global-option [global-option-value] <command> [<args>]'\n" +
                        "  see 'strongbox help <command>' for more information about ordering.",
                        globalOptions.get()));
            } else {
                System.err.println("See 'strongbox help'.");
            }

            System.exit(1);
        } catch (Exception e) {
            boolean stacktrace = stacktraceIsSet(parser.getMetadata(), args);

            if (!stacktrace) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
            throw e;
        }
    }

    /**
     * We check if the option is set here rather than in each command
     * to have a single place to catch all exceptions.
     *
     * Given that we first catch ParseArgumentsUnexpectedException we
     * do expect that parsing the same input again will not throw an exception.
     */
    private static boolean stacktraceIsSet(final GlobalMetadata globalMetadata, final String[] args) {
        try {
            Parser p = new Parser();
            ListMultimap<OptionMetadata, Object> options = p.parse(globalMetadata, args).getParsedOptions();

            for (Map.Entry<OptionMetadata, Collection<Object>> option : options.asMap().entrySet()) {
                OptionMetadata metadata = option.getKey();

                if (metadata.getOptionType() == OptionType.GLOBAL
                        && metadata.getOptions().contains("--stacktrace")) {
                    return option.getValue().contains(true);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to determine if the stacktrace should be shown. Please report this bug.");
        }

        return false;
    }
}
