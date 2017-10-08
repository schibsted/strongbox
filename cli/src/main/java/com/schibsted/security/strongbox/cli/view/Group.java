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

import com.schibsted.security.strongbox.cli.StrongboxCLI;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Help;
import io.airlift.airline.Option;

import java.util.Collections;

/**
 * CLI commands related to managing Strongbox Secrets Groups
 *
 * @author stiankri
 */
public class Group {

    @Command(name = "group-help", description = "Display help information for 'group'")
    public static class GroupHelp implements Runnable {
        @Override
        public void run() {
            Help.help(StrongboxCLI.globalMetadata, Collections.singletonList("group"));
        }
    }

    public static class GroupBaseCommand extends Global.BaseCommand {
        @Arguments(description = "Group", required=true)
        public String groupName;
    }

    @Command(name = "create", description = "Create a new Secrets Group")
    public static class Create extends GroupBaseCommand {
        @Option(name = "--storage-type", description = "Storage Backend to use {dynamodb, file}, DynamoDB is the default", required=false)
        public String storageType;

        @Option(name = "--path", description = "Is required when File is use as the Storage Backend", required=false)
        public String file;

        @Option(name = "--allow-key-reuse", description = "Useful if you want to restore a KMS key that is pending deletion or is disabled", required=false)
        public Boolean allowKeyReuse;

        @Override
        public void run() {
            renderer().render(groupModel().createGroup(groupName, storageType, file, allowKeyReuse));
        }
    }

    @Command(name = "delete", description = "Delete Secrets Group")
    public static class Delete extends GroupBaseCommand {
        @Option(name = "--force", description = "Do not ask for confirmation", required = false)
        Boolean force;

        @Override
        public void run() {
            groupModel().deleteGroup(groupName, force);
        }
    }

    @Command(name = "info", description = "Get info about Secret Group")
    public static class Info extends GroupBaseCommand {

        @Override
        public void run() {
            renderer().render(groupModel().groupInfo(groupName));
        }
    }

    @Command(name = "list", description = "List available Secrets Groups")
    public static class List extends Global.BaseCommand {
        @Override
        public void run() {
            renderer().render(groupModel().listGroup());
        }
    }

    public static class PolicyBaseCommand extends Global.BaseCommand {
        @Option(name = "--group", description = "Group", required=true)
        public String groupName;

        @Option(name = "--type", description = "Principal Type", required=false)
        public String principalType;

        @Arguments(description = "Principal", required=true)
        public String principal;
    }

    @Command(name = "attach-admin", description = "Attach AWS Principal to the Group's Admin Policy")
    public static class AttachAdmin extends PolicyBaseCommand {
        @Override
        public void run() {
            groupModel().attachAdmin(groupName, principal, principalType);
        }
    }

    @Command(name = "detach-admin", description = "Detach AWS Principal to the Group's Admin Policy")
    public static class DetachAdmin extends PolicyBaseCommand {
        @Override
        public void run() {
            groupModel().detachAdmin(groupName, principal, principalType);
        }
    }

    @Command(name = "attach-readonly", description = "Attach AWS Principal to Group's Readonly Policy")
    public static class AttachReadOnly extends PolicyBaseCommand {
        @Override
        public void run() {
            groupModel().attachReadOnly(groupName, principal, principalType);
        }
    }

    @Command(name = "detach-readonly", description = "Detach AWS Principal to the Group's Readonly Policy")
    public static class DetachReadOnly extends PolicyBaseCommand {
        @Override
        public void run() {
            groupModel().detachReadOnly(groupName, principal, principalType);
        }
    }

    @Command(name = "backup", description = "Backup Secrets Group State to File")
    public static class BackupCommand extends GroupBaseCommand {
        @Option(name = "--dst-path", description = "File to backup to", required=true)
        public String file;

        @Option(name = "--force", description = "Do not ask for confirmation", required = false)
        Boolean force;

        @Override
        public void run() {
            groupModel().backup(groupName, file, force);
        }
    }

    @Command(name = "restore", description = "Restore Secrets Group State from File")
    public static class RestoreCommand extends GroupBaseCommand {
        @Option(name = "--src-path", description = "File to restore from", required=true)
        public String file;

        @Option(name = "--force", description = "Do not ask for confirmation", required = false)
        Boolean force;

        @Override
        public void run() {
            groupModel().restore(groupName, file, force);
        }
    }

    @Command(name = "migrate", description = "Migrate to Storage Backend to a Different Type")
    public static class MigrateCommand extends GroupBaseCommand {
        @Option(name = "--storage-type", description = "Storage Backend to use {dynamodb, file}, DynamoDB is the default", required=true)
        public String storageType;

        @Option(name = "--path", description = "Is required when File is use as the Storage Backend", required=false)
        public String file;

        @Option(name = "--force", description = "Do not ask for confirmation", required = false)
        Boolean force;

        @Override
        public void run() {
            renderer().render(groupModel().migrate(groupName, storageType, file, force));
        }
    }
}
