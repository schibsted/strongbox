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

import com.schibsted.security.strongbox.cli.viewmodel.types.ListVersionsView;
import com.schibsted.security.strongbox.sdk.testing.SecretEntryMock;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.State;
import com.schibsted.security.strongbox.sdk.types.UserAlias;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.COMMENT;
import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.CREATED;
import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.MODIFIED;
import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.VERSION;
import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.STATE;
import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.loadExpectedValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author stiankri
 */
public class RenderListVersionsTest {
    SecretIdentifier secretIdentifier = new SecretIdentifier("mySecret");
    long version = 1;
    State state = State.ENABLED;
    ZonedDateTime createdTime = ZonedDateTime.of(2017,2,3,4,5,6,7, ZoneId.of("UTC"));
    ZonedDateTime modifiedTime = ZonedDateTime.of(2018,2,3,4,5,6,7, ZoneId.of("UTC"));
    ZonedDateTime notBefore = ZonedDateTime.of(2019,2,3,4,5,6,7, ZoneId.of("UTC"));
    ZonedDateTime notAfter = ZonedDateTime.of(2020,2,3,4,5,6,7, ZoneId.of("UTC"));
    UserAlias createdBy = new UserAlias("john.doe");
    UserAlias modifiedBy = new UserAlias("jane.doe");
    String comment = "some comment";

    byte[] dummyEncryptedPayload = {1,2,3,4,5};
    RawSecretEntry rawSecretEntry = new RawSecretEntry(secretIdentifier, version, state, Optional.of(notBefore), Optional.of(notAfter), dummyEncryptedPayload);
    ListVersionsView rawListVersionsView = new ListVersionsView(rawSecretEntry);

    RawSecretEntry rawSecretEntryPartial = new RawSecretEntry(secretIdentifier, version, state, Optional.empty(), Optional.empty(), dummyEncryptedPayload);
    ListVersionsView rawListVersionsViewPartial = new ListVersionsView(rawSecretEntryPartial);

    SecretEntry secretEntry = SecretEntryMock.builder()
            .secretIdentifier(secretIdentifier)
            .version(version)
            .secretValue("1234")
            .created(createdTime)
            .modified(modifiedTime)
            .state(State.ENABLED)
            .notBefore(notBefore)
            .notAfter(notAfter)
            .createdBy(createdBy)
            .modifiedBy(modifiedBy)
            .comment(comment)
            .build();

    ListVersionsView listVersionsView = new ListVersionsView(secretEntry);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(outputStream);

    @Test
    public void json_raw() throws IOException {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.JSON, printStream, null, null);
        renderer.render(Collections.singletonList(rawListVersionsView));

        String expectedResult = String.format("[ %s ]\n", loadExpectedValue("expected_list_versions_raw.json"));
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void json() throws IOException {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.JSON, printStream, null, null);
        renderer.render(Collections.singletonList(listVersionsView));

        String expectedResult = String.format("[ %s ]\n", loadExpectedValue("expected_list_versions.json"));
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void json_raw_partial() throws IOException {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.JSON, printStream, null, null);
        renderer.render(Collections.singletonList(rawListVersionsViewPartial));

        String expectedResult = String.format("[ %s ]\n", loadExpectedValue("expected_list_versions_raw_partial.json"));
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void text_raw() throws IOException {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.TEXT, printStream, null, null);
        renderer.render(Collections.singletonList(rawListVersionsView));

        String expectedResult = String.format("%d: [%s]\n", version, state.toString());
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void text() throws IOException {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.TEXT, printStream, null, null);
        renderer.render(Collections.singletonList(listVersionsView));

        String expectedResult = String.format("%d: [%s] [%s] - \"%s\"\n", version, "Sat Feb 3 2018 04:05:06 UTC", state.toString(), comment);
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void csv_version() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.CSV, printStream, VERSION, null);
        renderer.render(Collections.singletonList(listVersionsView));

        String expectedResult = String.format("%d\n", version);
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void csv() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.CSV, printStream, String.format("%s,%s,%s,%s,%s", VERSION, CREATED, MODIFIED, STATE, COMMENT), null);
        renderer.render(Collections.singletonList(listVersionsView));

        String expectedResult = String.format("%d,%s,%s,%s,%s\n", version, "1486094706", "1517630706", state.toString(), comment);
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void raw_version() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.RAW, printStream, VERSION, null);
        renderer.render(Collections.singletonList(listVersionsView));

        String expectedResult = String.format("%d", version);
        assertThat(outputStream.toString(), is(expectedResult));
    }
}
