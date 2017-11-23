/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import java.util.Optional;

/**
 * @author stiankri
 */
public final class SecretMetadata {
    public final SecretIdentifier secretIdentifier;
    public final long version;
    public final Optional<State> state;
    public final Optional<UserAlias> modifiedBy;
    public final Optional<Optional<UserData>> userData;
    public final Optional<Optional<Comment>> comment;

    // TODO consider making a builder
    public SecretMetadata(SecretIdentifier secretIdentifier,
                          long version,
                          Optional<State> state,
                          Optional<UserAlias> modifiedBy,
                          Optional<Optional<UserData>> userData,
                          Optional<Optional<Comment>> comment) {
        this.secretIdentifier = secretIdentifier;
        this.version = version;
        this.state = state;
        this.modifiedBy = modifiedBy;
        this.userData = userData;
        this.comment = comment;
    }
}
