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
