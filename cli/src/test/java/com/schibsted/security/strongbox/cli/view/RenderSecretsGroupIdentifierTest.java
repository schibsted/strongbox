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

import com.schibsted.security.strongbox.cli.viewmodel.types.SecretsGroupIdentifierView;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.loadExpectedValue;
import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.GROUP_NAME;
import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.GROUP_REGION_NAME;

/**
 * @author stiankri
 */
public class RenderSecretsGroupIdentifierTest {
    Region region = Region.EU_WEST_1;
    String groupName = "mygroup";
    SecretsGroupIdentifier group = new SecretsGroupIdentifier(region, groupName);
    SecretsGroupIdentifierView view = new SecretsGroupIdentifierView(group);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(outputStream);

    @Test
    public void json() throws IOException {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.JSON, printStream, null, null);
        renderer.render(Collections.singletonList(view));

        String expectedResult = String.format("[ %s ]\n", loadExpectedValue("expected_group_identifier.json"));
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void text() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.TEXT, printStream, null, null);
        renderer.render(view);

        String expectedResult = String.format("%s [%s]\n", groupName, group.region.name);
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void csv_name() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.CSV, printStream, GROUP_NAME, null);
        renderer.render(view);

        String expectedResult = String.format("%s\n", groupName);
        assertThat(outputStream.toString(), is(expectedResult));

    }

    @Test
    public void csv_name_region() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.CSV, printStream, String.format("%s,%s", GROUP_NAME, GROUP_REGION_NAME), null);
        renderer.render(view);

        String expectedResult = String.format("%s,%s\n", groupName, group.region.name);
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void raw() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.RAW, printStream, GROUP_NAME, null);
        renderer.render(view);

        String expectedResult = String.format("%s", groupName);
        assertThat(outputStream.toString(), is(expectedResult));
    }
}
