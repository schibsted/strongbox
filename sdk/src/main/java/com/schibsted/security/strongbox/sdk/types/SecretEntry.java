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

package com.schibsted.security.strongbox.sdk.types;

import com.google.common.base.MoreObjects;
import com.schibsted.security.strongbox.sdk.internal.encryption.BestEffortShred;
import com.schibsted.security.strongbox.sdk.internal.encryption.EncryptionPayload;

import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * @author stiankri
 * @author kvlees
 */
public class SecretEntry implements BestEffortShred {
    public final SecretIdentifier secretIdentifier;
    public final long version;
    public final SecretValue secretValue;
    public final ZonedDateTime created;
    public final ZonedDateTime modified;
    public final Optional<UserAlias> createdBy;
    public final Optional<UserAlias> modifiedBy;
    public final State state;
    public final Optional<ZonedDateTime> notBefore;
    public final Optional<ZonedDateTime> notAfter;
    public final Optional<Comment> comment;
    public final Optional<UserData> userData;

    public SecretEntry(EncryptionPayload encryptionPayload, RawSecretEntry rawSecretEntry) {
        this.secretIdentifier = rawSecretEntry.secretIdentifier;
        this.version = rawSecretEntry.version;
        this.state = rawSecretEntry.state;
        this.notBefore = rawSecretEntry.notBefore;
        this.notAfter = rawSecretEntry.notAfter;

        this.secretValue = encryptionPayload.value;
        this.userData = encryptionPayload.userData;
        this.created = encryptionPayload.created;
        this.modified = encryptionPayload.modified;
        this.createdBy = encryptionPayload.createdBy;
        this.modifiedBy = encryptionPayload.modifiedBy;
        this.comment = encryptionPayload.comment;
    }

    protected SecretEntry(SecretIdentifier secretIdentifier,
                       long version, SecretValue secretValue,
                       ZonedDateTime created,
                       ZonedDateTime modified,
                       Optional<UserAlias> createdBy,
                       Optional<UserAlias> modifiedBy,
                       State state,
                       Optional<ZonedDateTime> notBefore,
                       Optional<ZonedDateTime> notAfter,
                       Optional<Comment> comment,
                       Optional<UserData> userData) {
        this.secretIdentifier = secretIdentifier;
        this.version = version;
        this.secretValue = secretValue;
        this.created = created;
        this.modified = modified;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
        this.state = state;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.comment = comment;
        this.userData = userData;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("secretIdentifier", secretIdentifier)
                .add("version", version)
                .add("secretValue", secretValue)
                .add("created", created)
                .add("modified", modified)
                .add("state", state)
                .add("notBefore", notBefore)
                .add("notAfter", notAfter)
                .add("comment", comment)
                .add("userData", userData)
                .toString();
    }

    @Override
    public void bestEffortShred() {
        secretValue.bestEffortShred();
        userData.ifPresent(UserData::bestEffortShred);
        comment.ifPresent(Comment::bestEffortShred);
    }
}
