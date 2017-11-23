/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
