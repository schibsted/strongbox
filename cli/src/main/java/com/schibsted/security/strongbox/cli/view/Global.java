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
import io.airlift.airline.OptionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Global options for the CLI which are inherited by all other commands
 *
 * @author stiankri
 */
public class Global {
    public static class BaseCommand implements Runnable {
        private Optional<GroupModel> groupModel = Optional.empty();

        @io.airlift.airline.Option(type = OptionType.GLOBAL, name = Option.Constants.ASSUME_ROLE, description = Option.Constants.ASSUME_ROLE_DESCRIPTION)
        public String role;

        @io.airlift.airline.Option(type = OptionType.GLOBAL, name = Option.Constants.PROFILE, description = Option.Constants.PROFILE_DESCRIPTION)
        public String profile;

        @io.airlift.airline.Option(type = OptionType.GLOBAL, name = Option.Constants.REGION, description = Option.Constants.REGION_DESCRIPTION)
        public String region;

        @io.airlift.airline.Option(type = OptionType.GLOBAL, name = Option.Constants.OUTPUT, description = Option.Constants.OUTPUT_DESCRIPTION)
        public String outputFormat;

        @io.airlift.airline.Option(type = OptionType.GLOBAL, name = Option.Constants.OUTPUT_FIELD_NAMES, description = Option.Constants.OUTPUT_FIELD_NAMES_DESCRIPTION)
        public String fieldName;

        @io.airlift.airline.Option(type = OptionType.GLOBAL, name = Option.Constants.SPLIT_OUTPUT_INTO_FILES, description = Option.Constants.SPLIT_OUTPUT_INTO_FILES_DESCRIPTION)
        public String outputPath;

        @io.airlift.airline.Option(type = OptionType.GLOBAL, name = Option.Constants.AES_256, description = Option.Constants.AES_256_DESCRIPTION)
        public boolean useAES256;

        protected GroupModel groupModel() {
            if (!this.groupModel.isPresent()) {
                this.groupModel = Optional.of(new GroupModel(profile, role, region, useAES256, outputFormat, fieldName, outputPath));
            }
            return this.groupModel.get();
        }

        protected Renderer renderer() {
            return new Renderer(groupModel().getOutputFormat(), groupModel().getFieldName(), groupModel().getSaveToFilePath());
        }

        @Override
        public void run() {
        }
    }

    public enum Option {
        ASSUME_ROLE(Constants.ASSUME_ROLE, Constants.ASSUME_ROLE_DESCRIPTION),
        PROFILE(Constants.PROFILE, Constants.PROFILE_DESCRIPTION),
        REGION(Constants.REGION, Constants.REGION_DESCRIPTION),
        OUTPUT(Constants.OUTPUT, Constants.OUTPUT_DESCRIPTION),
        OUTPUT_FIELD_NAMES(Constants.OUTPUT_FIELD_NAMES, Constants.OUTPUT_FIELD_NAMES_DESCRIPTION),
        SPLIT_OUTPUT_INTO_FILES(Constants.SPLIT_OUTPUT_INTO_FILES, Constants.SPLIT_OUTPUT_INTO_FILES_DESCRIPTION),
        AES_256(Constants.AES_256, Constants.AES_256_DESCRIPTION);

        public final String name;
        public final String description;

        static Map<String, Option> optionsMap = new HashMap<>();

        static {
            for (Option option : values()) {
                optionsMap.put(option.name, option);
            }
        }

        Option(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public static boolean contains(String name) {
            return optionsMap.containsKey(name);
        }

        private static class Constants {
            static final String ASSUME_ROLE = "--assume-role";
            static final String ASSUME_ROLE_DESCRIPTION = "Assume AWS IAM Role";

            static final String PROFILE = "--profile";
            static final String PROFILE_DESCRIPTION = "Use AWS Profile";

            static final String REGION = "--region";
            static final String REGION_DESCRIPTION = "AWS Region, e.g. 'eu-west-1'";

            static final String OUTPUT = "--output";
            static final String OUTPUT_DESCRIPTION = "Output {text, json, raw}, text is default";

            static final String OUTPUT_FIELD_NAMES = "--output-field-names";
            static final String OUTPUT_FIELD_NAMES_DESCRIPTION = "When using --output raw, specify the field to output";

            static final String SPLIT_OUTPUT_INTO_FILES = "--split-output-into-files";
            static final String SPLIT_OUTPUT_INTO_FILES_DESCRIPTION = "Split each output element into a separate file";

            static final String AES_256 = "--use-aes-256-encryption";
            static final String AES_256_DESCRIPTION = "Use AES-256 rather than the default AES-128";
        }
    }
}
