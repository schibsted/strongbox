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

package com.schibsted.security.strongbox.sdk.testing;

import com.schibsted.security.strongbox.sdk.types.Comment;
import com.schibsted.security.strongbox.sdk.types.SecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretType;
import com.schibsted.security.strongbox.sdk.types.SecretValue;
import com.schibsted.security.strongbox.sdk.types.State;
import com.schibsted.security.strongbox.sdk.types.UserAlias;
import com.schibsted.security.strongbox.sdk.types.UserData;

import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * @author stiankri
 */
public class SecretEntryMock extends SecretEntry {
    private SecretEntryMock(SecretIdentifier secretIdentifier,
                           long version,
                           SecretValue secretValue,
                           ZonedDateTime created,
                           ZonedDateTime modified,
                           Optional<UserAlias> createdBy,
                           Optional<UserAlias> modifiedBy,
                           State state,
                           Optional<ZonedDateTime> notBefore,
                           Optional<ZonedDateTime> notAfter,
                           Optional<Comment> comment,
                           Optional<UserData> userData) {
        super(secretIdentifier, version, secretValue, created, modified, createdBy, modifiedBy, state, notBefore, notAfter, comment, userData);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SecretIdentifier secretIdentifier = null;
        private long version;
        private SecretValue secretValue = null;
        private ZonedDateTime created = null;
        private ZonedDateTime modified = null;
        private Optional<UserAlias> createdBy = Optional.empty();
        private Optional<UserAlias> modifiedBy = Optional.empty();
        private State state = null;
        private Optional<ZonedDateTime> notBefore = Optional.empty();
        private Optional<ZonedDateTime> notAfter = Optional.empty();
        private Optional<Comment> comment = Optional.empty();
        private Optional<UserData> userData = Optional.empty();

        public Builder() {

        }

        public SecretEntryMock build() {
            return new SecretEntryMock(secretIdentifier,
                    version,
                    secretValue,
                    created,
                    modified,
                    createdBy,
                    modifiedBy,
                    state,
                    notBefore,
                    notAfter,
                    comment,
                    userData);
        }

        public Builder secretIdentifier(SecretIdentifier secretIdentifier) {
            this.secretIdentifier = secretIdentifier;
            return this;
        }

        public Builder secretIdentifier(String secretIdentifier) {
            this.secretIdentifier = new SecretIdentifier(secretIdentifier);
            return this;
        }

        public Builder version(long version) {
            this.version = version;
            return this;
        }

        public Builder secretValue(SecretValue secretValue) {
            this.secretValue = secretValue;
            return this;
        }

        public Builder secretValue(String secretValue) {
            this.secretValue = new SecretValue(secretValue, SecretType.OPAQUE);
            return this;
        }

        public Builder secretValue(byte[] secretValue) {
            this.secretValue = new SecretValue(secretValue, SecretType.OPAQUE);
            return this;
        }

        public Builder created(ZonedDateTime created) {
            this.created = created;
            return this;
        }

        public Builder modified(ZonedDateTime modified) {
            this.modified = modified;
            return this;
        }

        public Builder createdBy(UserAlias createdBy) {
            this.createdBy = Optional.of(createdBy);
            return this;
        }

        public Builder modifiedBy(UserAlias modifiedBy) {
            this.modifiedBy = Optional.of(modifiedBy);
            return this;
        }

        public Builder state(State state) {
            this.state = state;
            return this;
        }

        public Builder notBefore(ZonedDateTime notBefore) {
            this.notBefore = Optional.of(notBefore);
            return this;
        }

        public Builder notAfter(ZonedDateTime notAfter) {
            this.notAfter = Optional.of(notAfter);
            return this;
        }

        public Builder comment(Comment comment) {
            this.comment = Optional.of(comment);
            return this;
        }

        public Builder comment(String comment) {
            this.comment = Optional.of(new Comment(comment));
            return this;
        }

        public Builder userData(UserData userData) {
            this.userData = Optional.of(userData);
            return this;
        }
    }
}
