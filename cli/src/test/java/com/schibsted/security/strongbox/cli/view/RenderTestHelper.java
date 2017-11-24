/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.view;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author stiankri
 */
public class RenderTestHelper {
    public static final String SECRET_VALUE = "secretValue.secretValue";
    public static final String VERSION = "version";
    public static final String SECRET_NAME = "secretIdentifier.name";
    public static final String ENCODING = "secretValue.encoding";

    public static final String GROUP_NAME = "name";
    public static final String GROUP_REGION_NAME = "region";

    public static final String SECRET_IDENTIFIER_NAME = "name";

    public static final String GROUP_INFO_SRN = "srn";

    public static final String CREATED = "created";
    public static final String MODIFIED = "modified";
    public static final String STATE = "state";
    public static final String COMMENT = "comment.comment";

    private static final String TEST_DATA_DIR = "src/test/resources";

    static String loadExpectedValue(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(TEST_DATA_DIR, fileName)));
    }
}
