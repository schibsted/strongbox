/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.viewmodel.types;

import com.schibsted.security.strongbox.sdk.internal.converter.FormattedTimestamp;
import com.google.common.collect.ImmutableMap;
import com.schibsted.security.strongbox.sdk.types.SecretEntry;

import java.util.Optional;

import java.util.Map;

/**
 * @author stiankri
 */
public class SecretEntryView implements View {
    public final SecretIdentifierView secretIdentifier;
    public final long version;
    public final SecretValueView secretValue;

    public final Long created;
    public final Long modified;
    public final Optional<UserAliasView> createdBy;
    public final Optional<UserAliasView> modifiedBy;
    public final String state;
    public final Optional<Long> notBefore;
    public final Optional<Long> notAfter;
    public final Optional<CommentView> comment;
    public final Optional<UserDataView> userData;

    private final BinaryString originalSecretValue;

    public SecretEntryView(SecretEntry entry) {
        this.secretIdentifier = new SecretIdentifierView(entry.secretIdentifier);
        version = entry.version;

        this.originalSecretValue = BinaryString.from(entry.secretValue);
        this.secretValue = new SecretValueView(entry.secretValue);

        this.created = FormattedTimestamp.epoch(entry.created);
        this.modified = FormattedTimestamp.epoch(entry.modified);
        this.createdBy = entry.createdBy.map(UserAliasView::new);
        this.modifiedBy = entry.modifiedBy.map(UserAliasView::new);
        this.state = entry.state.toString();
        this.notBefore = entry.notBefore.map(FormattedTimestamp::epoch);
        this.notAfter = entry.notAfter.map(FormattedTimestamp::epoch);
        this.comment = entry.comment.map(CommentView::new);
        this.userData = entry.userData.map(UserDataView::new);

        entry.bestEffortShred();
    }

    @Override
    public String toString() {
        return String.format("name: %s version: %s value: %s", secretIdentifier.name, version, secretValue.secretValue);
    }

    @Override
    public Map<String, BinaryString> toMap() {
        ImmutableMap.Builder<String, BinaryString> builder = ImmutableMap.builder();
        builder.put("secretIdentifier.name", new BinaryString(secretIdentifier.name));
        builder.put("version", new BinaryString(Long.toUnsignedString(version)));
        builder.put("secretValue.secretValue", this.originalSecretValue);
        builder.put("secretValue.encoding", new BinaryString(this.originalSecretValue.encoding.toString()));
        return builder.build();
    }

    @Override
    public String uniqueName() {
        return String.format("%s.%s", secretIdentifier.name, version);
    }
}
