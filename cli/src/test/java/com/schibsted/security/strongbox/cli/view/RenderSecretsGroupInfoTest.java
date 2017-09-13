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

import com.schibsted.security.strongbox.cli.viewmodel.types.SecretsGroupInfoView;
import com.schibsted.security.strongbox.sdk.internal.srn.SecretsGroupSRN;
import com.schibsted.security.strongbox.sdk.types.Principal;
import com.schibsted.security.strongbox.sdk.types.PrincipalType;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupInfo;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.GROUP_INFO_SRN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static com.schibsted.security.strongbox.cli.view.RenderTestHelper.loadExpectedValue;

/**
 * @author stiankri
 */
public class RenderSecretsGroupInfoTest {

    String account = "12345";
    Region region = Region.EU_WEST_1;
    String groupName = "mygroup";
    SecretsGroupIdentifier group = new SecretsGroupIdentifier(region, groupName);
    SecretsGroupSRN srn = new SecretsGroupSRN(account, group);

    String dummyEncryptorArn = "dummy:encryptor";
    String dummyStorageArn = "dummy:storage";
    String dummyAdminArn = "dummy:admin";
    String dummyReadOnlyArn = "dummy:readonly";

    Principal admin = new Principal(PrincipalType.ROLE, "role1");
    Principal readonly = new Principal(PrincipalType.ROLE, "role2");

    SecretsGroupInfo groupInfo = new SecretsGroupInfo(srn,
            Optional.of(dummyEncryptorArn),
            Optional.of(dummyStorageArn),
            Optional.of(dummyAdminArn),
            Optional.of(dummyReadOnlyArn),
            Collections.singletonList(admin),
            Collections.singletonList(readonly));
    SecretsGroupInfoView view = new SecretsGroupInfoView(groupInfo);

    SecretsGroupInfo groupInfoPartial = new SecretsGroupInfo(srn,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            new ArrayList<>(),
            new ArrayList<>());
    SecretsGroupInfoView viewPartial = new SecretsGroupInfoView(groupInfoPartial);


    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(outputStream);

    @Test
    public void json() throws IOException {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.JSON, printStream, null, null);
        renderer.render(view);

        String expectedResult = String.format("%s\n", loadExpectedValue("expected_group_info.json"));
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void json_partial() throws IOException {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.JSON, printStream, null, null);
        renderer.render(viewPartial);

        String expectedResult = String.format("%s\n", loadExpectedValue("expected_group_info_partial.json"));
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void text() throws IOException {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.TEXT, printStream, null, null);
        renderer.render(view);

        String expectedResult = String.format("%s\n", loadExpectedValue("expected_group_info.txt"));
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void text_partial() throws IOException {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.TEXT, printStream, null, null);
        renderer.render(viewPartial);

        String expectedResult = String.format("%s\n", loadExpectedValue("expected_group_info_partial.txt"));
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void csv() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.CSV, printStream, GROUP_INFO_SRN, null);
        renderer.render(viewPartial);

        String expectedResult = String.format("%s\n", srn.toSrn());
        assertThat(outputStream.toString(), is(expectedResult));
    }

    @Test
    public void raw() {
        outputStream.reset();

        Renderer renderer = new Renderer(OutputFormat.RAW, printStream, GROUP_INFO_SRN, null);
        renderer.render(viewPartial);

        String expectedResult = String.format("%s", srn.toSrn());
        assertThat(outputStream.toString(), is(expectedResult));
    }
}
