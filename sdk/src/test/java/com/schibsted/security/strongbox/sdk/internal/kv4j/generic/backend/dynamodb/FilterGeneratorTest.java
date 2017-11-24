/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.kv4j.generic.backend.dynamodb;

import com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend.Parser;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend.RSEF;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;

import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.name;
import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.notAfter;
import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.notBefore;
import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.version;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author stiankri
 */
public class FilterGeneratorTest {
    private static final Logger log = LoggerFactory.getLogger(FilterGeneratorTest.class);

    @Test
    public void test() {
        assertThat(FilterGenerator.convertNumberToLetters(0), is("a"));
        assertThat(FilterGenerator.convertNumberToLetters(1), is("b"));
        assertThat(FilterGenerator.convertNumberToLetters(26-1), is("z"));

        assertThat(FilterGenerator.convertNumberToLetters(26), is("ba"));
        assertThat(FilterGenerator.convertNumberToLetters(26+1), is("bb"));
        assertThat(FilterGenerator.convertNumberToLetters(26*26-1), is("zz"));

        assertThat(FilterGenerator.convertNumberToLetters(26*26), is("baa"));
        assertThat(FilterGenerator.convertNumberToLetters(26*26+1), is("bab"));
        assertThat(FilterGenerator.convertNumberToLetters(26*26*26-1), is("zzz"));
    }

    @Test
    public void test2() {

        ZonedDateTime notBeforeValue = ZonedDateTime.now();

        RSEF.AttributeCondition condition = notAfter.isPresent()
                .AND(RSEF.NOT(notAfter.get().eq(notBeforeValue)))
                .OR(notAfter.get().eq(notBeforeValue))
                .AND(notBefore.get().eq(notAfter.get()));

        RSEF.ParsedAttributeCondition ast = Parser.createAST(condition);

        FilterGenerator filterGenerator = new FilterGenerator();
        FilterGenerator.Filter filter = filterGenerator.process(ast, Config.converters);

        log.info(filter.filterExpression);
    }

    @Test
    public void test3() {
        RSEF.AttributeCondition condition = notBefore.isNotPresent();

        RSEF.ParsedAttributeCondition ast = Parser.createAST(condition);

        FilterGenerator filterGenerator = new FilterGenerator();
        FilterGenerator.Filter filter = filterGenerator.process(ast, Config.converters);

        log.info(filter.filterExpression);
    }

    @Test
    public void test4() {
        SecretIdentifier secretIdentifier = new SecretIdentifier("MySecret");
        long myVersion = 1;
        RSEF.KeyCondition condition = name.eq(secretIdentifier).AND(version.le(myVersion));

        RSEF.ParsedKeyCondition ast = Parser.createAST(condition);

        KeyExpressionGenerator generator = new KeyExpressionGenerator();
        KeyExpressionGenerator.KeyCondition keyCondition = generator.process(ast, Config.converters);

        log.info(keyCondition.keyConditionExpression);
    }
}
