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

import com.schibsted.security.strongbox.sdk.internal.encryption.BestEffortShred;

import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * @author stiankri
 */
public final class NewSecretEntry implements BestEffortShred {
    public final SecretIdentifier secretIdentifier;
    public final SecretValue secretValue;
    public final Optional<UserData> userData;
    public final State state;
    public final Optional<ZonedDateTime> notBefore;
    public final Optional<ZonedDateTime> notAfter;
    public final Optional<UserAlias> createdBy;
    public final Optional<Comment> comment;

    // TODO consider making a builder
    public NewSecretEntry(SecretIdentifier secretIdentifier,
                          SecretValue secretValue,
                          State state,
                          Optional<UserAlias> createdBy,
                          Optional<ZonedDateTime> notBefore,
                          Optional<ZonedDateTime> notAfter,
                          Optional<Comment> comment,
                          Optional<UserData> userData) {
        this.secretIdentifier = secretIdentifier;
        this.secretValue = secretValue;
        this.userData = userData;
        this.state = state;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.createdBy = createdBy;
        this.comment = comment;
    }

    public NewSecretEntry(SecretIdentifier secretIdentifier,
                          SecretValue secretValue,
                          State state,
                          Optional<ZonedDateTime> notBefore,
                          Optional<ZonedDateTime> notAfter,
                          Optional<Comment> comment) {
        this(secretIdentifier,
                secretValue,
                state,
                Optional.empty(),
                notBefore,
                notAfter,
                comment,
                Optional.empty()
        );
    }

    public NewSecretEntry(SecretIdentifier secretIdentifier, SecretValue secretValue, State state) {
        this(secretIdentifier,
                secretValue,
                state,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    @Override
    public void bestEffortShred() {
        secretValue.bestEffortShred();
        userData.ifPresent(UserData::bestEffortShred);
    }
}
