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

package com.schibsted.security.strongbox.cli.mfa;

import com.amazonaws.auth.BasicSessionCredentials;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Used for serialization and deserialization of session caches located in ~/.aws/cli/cache/
 *
 * @author stiankri
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cache {
    @JsonProperty("AssumedRoleUser")
    public final AssumedUserRole assumedUserRole;

    @JsonProperty("Credentials")
    public final Credentials credentials;

    public Cache(final String arn,
                 final String assumedRoleId,
                 final BasicSessionCredentials credentials,
                 final ZonedDateTime expiration) {
        this.assumedUserRole = new AssumedUserRole(arn, assumedRoleId);
        this.credentials = new Credentials(credentials, expiration);
    }

    public Cache(@JsonProperty("AssumedRoleUser") final AssumedUserRole assumedUserRole,
                 @JsonProperty("Credentials") final Credentials credentials) {
        this.assumedUserRole = assumedUserRole;
        this.credentials = credentials;
    }

    public static class AssumedUserRole {
        @JsonProperty("Arn")
        public final String arn;

        @JsonProperty("AssumedRoleId")
        public final String assumedRoleId;

        public AssumedUserRole(@JsonProperty("Arn") final String arn,
                               @JsonProperty("AssumedRoleId") final String assumedRoleId) {
            this.arn = arn;
            this.assumedRoleId = assumedRoleId;
        }
    }

    public static class Credentials {
        @JsonProperty("AccessKeyId")
        public final String accessKeyId;

        @JsonProperty("Expiration")
        public final  String expiration;

        @JsonProperty("SecretAccessKey")
        public final String secretAccessKey;

        @JsonProperty("SessionToken")
        public final String sessionToken;

        public Credentials(@JsonProperty("AccessKeyId") final String accessKeyId,
                           @JsonProperty("Expiration") final String expiration,
                           @JsonProperty("SecretAccessKey") final String secretAccessKey,
                           @JsonProperty("SessionToken") final String sessionToken) {
            this.accessKeyId = accessKeyId;
            this.expiration = expiration;
            this.secretAccessKey = secretAccessKey;
            this.sessionToken = sessionToken;
        }

        public Credentials(final BasicSessionCredentials credentials,
                           final ZonedDateTime expiration) {
            this.accessKeyId = credentials.getAWSAccessKeyId();
            this.secretAccessKey = credentials.getAWSSecretKey();
            this.sessionToken = credentials.getSessionToken();
            this.expiration = expiration.format(DateTimeFormatter.ISO_INSTANT);
        }

        ZonedDateTime getExpiration() {
            return ZonedDateTime.parse(this.expiration, DateTimeFormatter.ISO_DATE_TIME);
        }
    }
}
