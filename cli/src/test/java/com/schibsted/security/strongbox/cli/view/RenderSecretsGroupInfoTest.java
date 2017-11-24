/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
