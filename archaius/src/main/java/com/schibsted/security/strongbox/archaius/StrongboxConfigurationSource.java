/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.archaius;

import com.netflix.config.PollResult;
import com.netflix.config.PolledConfigurationSource;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config;
import com.schibsted.security.strongbox.sdk.SecretsGroup;

import java.util.stream.Collectors;

/**
 * @author stiankri
 */
public class StrongboxConfigurationSource implements PolledConfigurationSource {
    private final SecretsGroup secretsGroup;

    public StrongboxConfigurationSource(SecretsGroup secretsGroup) {
        this.secretsGroup = secretsGroup;
    }

    @Override
    public PollResult poll(boolean initial, Object checkPoint) throws Exception {
        return PollResult.createFull(
                secretsGroup.stream().filter(Config.active()).reverse().uniquePrimaryKey().toJavaStream().
                collect(Collectors.toMap(e -> secretsGroup.srn(e.secretIdentifier).toSrn(), e -> e.toJsonBlob())
                ));
    }

}
