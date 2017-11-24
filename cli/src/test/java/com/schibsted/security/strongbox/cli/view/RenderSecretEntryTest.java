/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.view;

import com.schibsted.security.strongbox.cli.viewmodel.types.SecretEntryView;
import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;
import com.schibsted.security.strongbox.sdk.internal.converter.SecretValueConverter;
import com.schibsted.security.strongbox.sdk.testing.SecretEntryMock;
import com.schibsted.security.strongbox.sdk.types.Comment;
import com.schibsted.security.strongbox.sdk.types.Encoding;
import com.schibsted.security.strongbox.sdk.types.SecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretType;
import com.schibsted.security.strongbox.sdk.types.State;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;

import com.schibsted.security.strongbox.sdk.types.UserAlias;
import com.schibsted.security.strongbox.sdk.types.UserData;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.loadExpectedValue;
import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.SECRET_NAME;
import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.SECRET_VALUE;
import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.VERSION;
import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.ENCODING;

/**
 * @author stiankri
 */
public class RenderSecretEntryTest {
    ZonedDateTime createdTime = ZonedDateTime.of(2017,2,3,4,5,6,7, ZoneId.of("UTC"));
    ZonedDateTime modifiedTime = ZonedDateTime.of(2018,2,3,4,5,6,7, ZoneId.of("UTC"));
    ZonedDateTime notBefore = ZonedDateTime.of(2019,2,3,4,5,6,7, ZoneId.of("UTC"));
    ZonedDateTime notAfter = ZonedDateTime.of(2020,2,3,4,5,6,7, ZoneId.of("UTC"));

    UserAlias createdBy = new UserAlias("john.doe");
    UserAlias modifiedBy = new UserAlias("jane.doe");
    Comment comment = new Comment("some comment");
    UserData userData = new UserData("user data".getBytes());

    String secretName = "mySecret";
    String secretValue = "1234";
    long version = 1;
    SecretEntryMock.Builder secretEntryBuilder = SecretEntryMock.builder()
            .secretIdentifier(secretName)
            .version(version)
            .secretValue(secretValue)
            .created(createdTime)
            .modified(modifiedTime)
            .state(State.ENABLED);

    SecretEntry secretEntry = secretEntryBuilder.build();
    SecretEntryView secretEntryView = new SecretEntryView(secretEntry);

    SecretEntry secretEntryNewline = secretEntryBuilder.secretValue("value,with,comma,\nnewline, and \" quote").build();
    SecretEntryView getSecretEntryViewNewline = new SecretEntryView(secretEntryNewline);



    byte[] secretValueBinary = {(byte) 0xc3, 0x28, 0, 1};
    // The value is bestEffortShredded in the SecretEntryView
    byte[] secretValueBinaryToBeConsumed = Arrays.copyOf(secretValueBinary, secretValueBinary.length);


    String secretNameBinary = "myBinarySecret";
    SecretEntry secretEntryBinary = SecretEntryMock.builder()
            .secretIdentifier(secretNameBinary)
            .version(version)
            .secretValue(SecretValueConverter.inferEncoding(secretValueBinaryToBeConsumed, SecretType.OPAQUE))
            .created(createdTime)
            .modified(modifiedTime)
            .state(State.ENABLED)
            .notBefore(notBefore)
            .notAfter(notAfter)
            .createdBy(createdBy)
            .modifiedBy(modifiedBy)
            .comment(comment)
            .userData(userData)
            .build();
    SecretEntryView secretEntryViewBinary = new SecretEntryView(secretEntryBinary);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(outputStream);

    @Test
    public void raw_secret_value_utf8() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.RAW, printStream, SECRET_VALUE, null);
        renderer.render(secretEntryView);

        assertThat(secretEntry.secretValue.encoding, is(Encoding.UTF8));
        assertThat(outputStream.toByteArray(), equalTo(secretValue.getBytes()));
    }

    @Test
    public void raw_secret_value_binary() {
        outputStream.reset();


        Renderer renderer = new Renderer(OutputFormat.RAW, printStream, SECRET_VALUE, null);
        renderer.render(secretEntryViewBinary);

        assertThat(secretEntryBinary.secretValue.encoding, is(Encoding.BINARY));
        assertThat(outputStream.toByteArray(), equalTo(secretValueBinary));
    }

    @Test
    public void json_secret_entry_utf8() throws IOException {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.JSON, printStream, null, null);
        renderer.render(Collections.singletonList(secretEntryView));

        String expected = String.format("[ %s ]\n", loadExpectedValue("expected_secret_entry.json"));
        assertThat(outputStream.toString(), equalTo(expected));
    }

    @Test
    public void json_secret_entry_binary() throws IOException {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.JSON, printStream, null, null);
        renderer.render(Collections.singletonList(secretEntryViewBinary));

        String expected = String.format("[ %s ]\n", loadExpectedValue("expected_secret_entry_binary.json"));
        assertThat(outputStream.toString(), equalTo(expected));
    }

    @Test
    public void json_multiple_secret_entries() throws IOException {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.JSON, printStream, null, null);
        renderer.render(Arrays.asList(secretEntryView, secretEntryViewBinary));

        String expected = String.format("[ %s, %s ]\n", loadExpectedValue("expected_secret_entry.json"), loadExpectedValue("expected_secret_entry_binary.json"));
        assertThat(outputStream.toString(), equalTo(expected));
    }

    @Test
    public void text_secret_value_utf8() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.TEXT, printStream, SECRET_VALUE, null);
        renderer.render(secretEntryView);

        String expectedResult = String.format("%s\n", textLine(secretName, version, secretValue));
        assertThat(outputStream.toString(), equalTo(expectedResult));
    }

    @Test
    public void text_secret_value_binary() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.TEXT, printStream, SECRET_VALUE, null);
        renderer.render(secretEntryViewBinary);

        String expectedResult = String.format("%s\n", textLine(secretNameBinary, version, Encoder.base64encode(secretValueBinary)));
        assertThat(outputStream.toString(), equalTo(expectedResult));
    }

    @Test
    public void text_multiple_secret_value() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.TEXT, printStream, String.format("%s,%s", SECRET_NAME, SECRET_VALUE), null);
        renderer.render(Arrays.asList(secretEntryView, secretEntryViewBinary));

        String expectedResult = String.format("%s\n%s\n", textLine(secretName, version, secretValue), textLine(secretNameBinary, version, Encoder.base64encode(secretValueBinary)));
        assertThat(outputStream.toString(), equalTo(expectedResult));
    }

    private String textLine(String name, long version, String value) {
        return String.format("name: %s version: %d value: %s", name, version, value);
    }

    @Test
    public void csv_secret_value() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.CSV, printStream, SECRET_VALUE, null);
        renderer.render(secretEntryView);

        String expectedResult = String.format("%s\n", secretValue);
        assertThat(outputStream.toString(), equalTo(expectedResult));
    }

    @Test
    public void csv_ordering_1() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.CSV, printStream, String.format("%s,%s", SECRET_NAME, SECRET_VALUE), null);
        renderer.render(secretEntryView);

        String expectedResult = String.format("%s,%s\n", secretName, secretValue);
        assertThat(outputStream.toString(), equalTo(expectedResult));
    }

    @Test
    public void csv_ordering_2() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.CSV, printStream, String.format("%s,%s", SECRET_VALUE, SECRET_NAME), null);
        renderer.render(secretEntryView);

        String expectedResult = String.format("%s,%s\n", secretValue, secretName);
        assertThat(outputStream.toString(), equalTo(expectedResult));
    }

    @Test
    public void csv_secret_name_version_value_encoding_utf8() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.CSV, printStream, String.format("%s,%s,%s,%s", SECRET_NAME, VERSION, SECRET_VALUE, ENCODING), null);
        renderer.render(secretEntryView);

        String expectedResult = String.format("%s,%d,%s,%s\n", secretName, version, secretValue, "utf8");
        assertThat(outputStream.toString(), equalTo(expectedResult));
    }

    @Test
    public void csv_secret_name_version_value_encoding_binary() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.CSV, printStream, String.format("%s,%s,%s,%s", SECRET_NAME, VERSION, SECRET_VALUE, ENCODING), null);
        renderer.render(secretEntryViewBinary);

        String expectedResult = String.format("%s,%d,%s,%s\n", secretNameBinary, version, Encoder.base64encode(secretValueBinary), "binary");
        assertThat(outputStream.toString(), equalTo(expectedResult));
    }

    @Test
    public void csv_multiple_name_value() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.CSV, printStream, String.format("%s,%s", SECRET_NAME, SECRET_VALUE), null);
        renderer.render(Arrays.asList(secretEntryView, secretEntryViewBinary));

        String expectedResult = String.format("%s,%s\n%s,%s\n", secretName, secretValue, secretNameBinary, Encoder.base64encode(secretValueBinary));
        assertThat(outputStream.toString(), equalTo(expectedResult));
    }

    @Test
    public void csv_newline_comma() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.CSV, printStream, String.format("%s,%s", SECRET_NAME, SECRET_VALUE), null);
        renderer.render(Arrays.asList(getSecretEntryViewNewline));

        String expectedResult = String.format("%s,\"value,with,comma,\nnewline, and \"\" quote\"\n", secretName);
        assertThat(outputStream.toString(), equalTo(expectedResult));
    }
}
