/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.config.credentials;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.securitytoken.model.AssumedRoleUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.security.strongbox.sdk.types.ProfileIdentifier;
import com.schibsted.security.strongbox.sdk.types.arn.RoleARN;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Share AWS IAM assume role caches with the AWS CLI
 *
 * @author stiankri
 */
public class SessionCache {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final int EXPIRATION_THRESHOLD_IN_SECONDS = 60;

    private final RoleARN roleToAssume;
    private final ProfileIdentifier profile;
    private final File file;

    public SessionCache(final ProfileIdentifier profile, final RoleARN roleToAssume) {
        this.profile = profile;
        this.roleToAssume = roleToAssume;
        this.file = resolveFile();
    }

    public Optional<BasicSessionCredentials> load() {
        if (!file.exists()) {
            return Optional.empty();
        }

        try {
            SessionCacheSchema cache = objectMapper.readValue(file, SessionCacheSchema.class);

            if (ZonedDateTime.now().plusSeconds(EXPIRATION_THRESHOLD_IN_SECONDS).isBefore(cache.credentials.getExpiration())) {
                return Optional.of(new BasicSessionCredentials(cache.credentials.accessKeyId, cache.credentials.secretAccessKey, cache.credentials.sessionToken));
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to load session cache from '%s'", file.getAbsolutePath()), e);
        }
    }

    public void save(final AssumedRoleUser assumedRoleUser, final BasicSessionCredentials credentials, final ZonedDateTime expiration) {
        resolveCacheDirectory().mkdirs();

        SessionCacheSchema cache = new SessionCacheSchema(assumedRoleUser.getArn(), assumedRoleUser.getAssumedRoleId(), credentials, expiration);

        try {
            objectMapper.writeValue(file, cache);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to save session cache to '%s'", file.getAbsolutePath()), e);
        }
    }

    File resolveFile() {
        return new File(resolveCacheDirectory(), resolveFileName());
    }

    File resolveCacheDirectory() {
        return new File(System.getProperty("user.home") + "/.aws/cli/cache/");
    }

    String resolveFileName() {
        return String.format("%s--%s.json", profile.name, roleToAssume.toArn().replace(':', '_').replace('/', '-'));
    }
}
