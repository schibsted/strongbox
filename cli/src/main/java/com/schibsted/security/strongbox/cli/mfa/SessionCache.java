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
import com.amazonaws.services.securitytoken.model.AssumedRoleUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.security.strongbox.sdk.types.ProfileIdentifier;

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

    private final String roleArnToAssume;
    private final ProfileIdentifier profile;
    private final File file;

    public SessionCache(final ProfileIdentifier profile, final String roleArnToAssume) {
        this.profile = profile;
        this.roleArnToAssume = roleArnToAssume;
        this.file = resolveFile();
    }

    public Optional<BasicSessionCredentials> load() {
        if (!file.exists()) {
            return Optional.empty();
        }

        try {
            Cache cache = objectMapper.readValue(file, Cache.class);

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

        Cache cache = new Cache(assumedRoleUser.getArn(), assumedRoleUser.getAssumedRoleId(), credentials, expiration);

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
        return String.format("%s--%s.json", profile.name, roleArnToAssume.replace(':', '_').replace('/', '-'));
    }
}
