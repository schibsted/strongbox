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
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.internal.converter.FormattedTimestamp;
import com.schibsted.security.strongbox.sdk.types.SecretEntry;

import java.util.Map;
import java.util.Optional;

/**
 * A hybrid between SecretEntry and RawSecretEntry. How much of the data that
 * is populated depends on if the flag to decrypt is set.
 *
 * @author stiankri
 */
public class ListVersionsView implements View {
    public static final String UNKNOWN = "<not present>";

    public final long version;
    public final Optional<Long> created;
    public final Optional<Long> modified;
    public final Optional<UserAliasView> createdBy;
    public final Optional<UserAliasView> modifiedBy;
    public final String state;
    public final Optional<Long> notBefore;
    public final Optional<Long> notAfter;
    public final Optional<CommentView> comment;

    public ListVersionsView(RawSecretEntry entry) {
        this.version = entry.version;
        this.created = Optional.empty();
        this.modified = Optional.empty();
        this.createdBy = Optional.empty();
        this.modifiedBy = Optional.empty();
        this.state = entry.state.toString();
        this.notBefore = entry.notBefore.map(FormattedTimestamp::epoch);
        this.notAfter = entry.notAfter.map(FormattedTimestamp::epoch);
        this.comment = Optional.empty();

        entry.bestEffortShred();
    }

    public ListVersionsView(SecretEntry entry) {
        this.version = entry.version;
        this.created = Optional.of(FormattedTimestamp.epoch(entry.created));
        this.modified = Optional.of(FormattedTimestamp.epoch(entry.modified));
        this.createdBy = entry.createdBy.map(UserAliasView::new);
        this.modifiedBy = entry.modifiedBy.map(UserAliasView::new);
        this.state = entry.state.toString();
        this.notBefore = entry.notBefore.map(FormattedTimestamp::epoch);
        this.notAfter = entry.notAfter.map(FormattedTimestamp::epoch);
        this.comment = entry.comment.map(CommentView::new);

        entry.bestEffortShred();
    }

    @Override
    public String toString() {
        StringBuilder ss = new StringBuilder();

        ss.append(String.format("%d:", version));

        if (modified.isPresent()) {
            ss.append(String.format(" [%s]", FormattedTimestamp.toHumanReadable(modified.get())));
        }

        ss.append(String.format(" [%s]", state));

        if (comment.isPresent()) {
            ss.append(String.format(" - \"%s\"", comment.get().comment));
        }
        return ss.toString();
    }

    @Override
    public Map<String, BinaryString> toMap() {
        ImmutableMap.Builder<String, BinaryString> builder = ImmutableMap.builder();
        builder.put("version", new BinaryString(Long.toUnsignedString(version)));

        builder.put("created", created.isPresent() ? new BinaryString(Long.toUnsignedString(created.get())) : new BinaryString(""));
        builder.put("modified", modified.isPresent() ? new BinaryString(Long.toUnsignedString(modified.get())) : new BinaryString(""));

        builder.put("state", new BinaryString(state));

        builder.put("comment.comment", comment.isPresent() ? new BinaryString(comment.get().comment) : new BinaryString(""));

        return builder.build();
    }

    @Override
    public String uniqueName() {
        return Long.toUnsignedString(version);
    }
}
