/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
