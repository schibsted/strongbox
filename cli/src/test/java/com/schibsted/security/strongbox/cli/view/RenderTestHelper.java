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
