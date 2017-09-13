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
