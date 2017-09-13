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

package com.schibsted.security.strongbox.cli.viewmodel.types;

import com.google.common.collect.ImmutableMap;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author stiankri
 */
public class SecretsGroupInfoView implements View {
    public static final String MISSING = "<not present>";
    public final String srn;
    public final SecretsGroupIdentifierView groupIdentifier;
    public final Optional<String> encryptorArn;
    public final Optional<String> storageArn;
    public final Optional<String> adminPolicyArn;
    public final Optional<String> readOnlyPolicyArn;
    public final List<PrincipalView> admin;
    public final List<PrincipalView> readOnly;

    public SecretsGroupInfoView(SecretsGroupInfo groupInfo) {
        this.srn = groupInfo.srn.toSrn();
        this.groupIdentifier = new SecretsGroupIdentifierView(groupInfo.srn.groupIdentifier);
        this.encryptorArn = groupInfo.encryptorArn;
        this.storageArn = groupInfo.storageArn;
        this.adminPolicyArn = groupInfo.adminPolicyArn;
        this.readOnlyPolicyArn = groupInfo.readOnlyPolicyArn;
        this.admin = groupInfo.admin.stream().map(PrincipalView::new).collect(Collectors.toList());;
        this.readOnly = groupInfo.readOnly.stream().map(PrincipalView::new).collect(Collectors.toList());;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SRN: " + srn + "\n");
        sb.append("Group Name: " + groupIdentifier.name + "\n");
        sb.append("Region: " + groupIdentifier.region + "\n");

        sb.append("KMS ARN: " + (encryptorArn.isPresent() ? encryptorArn.get() : MISSING) + "\n");
        sb.append("Storage ARN: " + (storageArn.isPresent() ? storageArn.get() : MISSING) + "\n");
        sb.append("Admin Policy ARN: " + (adminPolicyArn.isPresent() ? adminPolicyArn.get() : MISSING) + "\n");
        sb.append("Readonly Policy ARN: " + (readOnlyPolicyArn.isPresent() ? readOnlyPolicyArn.get() : MISSING) + "\n");

        String adminLabel = "Admin Principals";
        if (admin.size() > 0) {
            sb.append(String.format("%s:\n", adminLabel));
            admin.forEach(p -> sb.append(String.format("  %s: %s\n", p.type, p.name)));
        } else {
            sb.append(String.format("%s: %s\n", adminLabel, MISSING));
        }

        String readOnlyLabel = "Read Only Principals";
        if (admin.size() > 0) {
            sb.append(String.format("%s:\n", readOnlyLabel));
            readOnly.forEach(p -> sb.append(String.format("  %s: %s\n", p.type, p.name)));
        } else {
            sb.append(String.format("%s: %s\n", readOnlyLabel, MISSING));
        }

        return sb.toString();
    }

    @Override
    public Map<String, BinaryString> toMap() {
        ImmutableMap.Builder<String, BinaryString> builder = ImmutableMap.builder();
        builder.put("srn", new BinaryString(srn));
        return builder.build();
    }

    @Override
    public String uniqueName() {
        return srn;
    }
}
