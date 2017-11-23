/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.config.credentials;

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
public class SessionCacheSchema {
    @JsonProperty("AssumedRoleUser")
    public final AssumedUserRole assumedUserRole;

    @JsonProperty("Credentials")
    public final Credentials credentials;

    public SessionCacheSchema(final String arn,
                              final String assumedRoleId,
                              final BasicSessionCredentials credentials,
                              final ZonedDateTime expiration) {
        this.assumedUserRole = new AssumedUserRole(arn, assumedRoleId);
        this.credentials = new Credentials(credentials, expiration);
    }

    public SessionCacheSchema(@JsonProperty("AssumedRoleUser") final AssumedUserRole assumedUserRole,
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
