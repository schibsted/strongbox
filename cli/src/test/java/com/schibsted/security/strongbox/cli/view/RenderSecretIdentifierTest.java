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

import com.schibsted.security.strongbox.cli.viewmodel.types.SecretIdentifierView;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.loadExpectedValue;
import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.SECRET_IDENTIFIER_NAME;

/**
 * @author stiankri
 */
public class RenderSecretIdentifierTest {
    String name = "mySecret";
    SecretIdentifier secretIdentifier = new SecretIdentifier(name);
    SecretIdentifierView view = new SecretIdentifierView(secretIdentifier);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(outputStream);

    @Test
    public void json() throws IOException {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.JSON, printStream, null, null);
        renderer.render(Collections.singletonList(view));

        String expectedResult = String.format("[ %s ]\n", loadExpectedValue("expected_secret_identifier.json"));
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void text() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.TEXT, printStream, null, null);
        renderer.render(Collections.singletonList(view));

        String expectedResult = String.format("%s\n", name);
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void csv() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.CSV, printStream, SECRET_IDENTIFIER_NAME, null);
        renderer.render(Collections.singletonList(view));

        String expectedResult = String.format("%s\n", name);
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void raw() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.RAW, printStream, SECRET_IDENTIFIER_NAME, null);
        renderer.render(view);

        String expectedResult = String.format("%s", name);
        assertThat(outputStream.toString(), is(expectedResult));
    }
}
