/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
