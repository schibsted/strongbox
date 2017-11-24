/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend;

import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * @author stiankri
 */
public class Parser {
    public static <S> RSEF.ParsedAttributeCondition<S> createAST(RSEF.AttributeCondition root) {
        if (root instanceof RSEF.CompositeAttributeCondition) {
            RSEF.CompositeAttributeCondition current = (RSEF.CompositeAttributeCondition)root;

            LinkedList<RSEF.ParsedAttributeCondition> parsedConditionals = current.conditionals.stream().map(Parser::createAST).collect(Collectors.toCollection(LinkedList::new));

            int numTokens = current.logicalOperatorTokens.size();

            int offset = 0;
            for (int i = 0; i<numTokens; ++i) {
                RSEF.LogicalOperatorToken token = current.logicalOperatorTokens.get(offset);
                if (token == RSEF.LogicalOperatorToken.AND) {
                    RSEF.AndOperator tip = new RSEF.AndOperator<S>(parsedConditionals.get(offset), parsedConditionals.get(offset+1));
                    parsedConditionals.remove(offset);
                    parsedConditionals.remove(offset);
                    parsedConditionals.add(offset, tip);
                    current.logicalOperatorTokens.remove(offset);
                } else {
                    ++offset;
                }
            }

            for (RSEF.LogicalOperatorToken token : current.logicalOperatorTokens) {
                RSEF.ParsedAttributeCondition<S> left = parsedConditionals.poll();
                RSEF.ParsedAttributeCondition<S> right = parsedConditionals.poll();

                RSEF.OrOperator tip = new RSEF.OrOperator<S>(left, right);
                parsedConditionals.push(tip);
            }
            return parsedConditionals.getFirst();
        }
        // TODO: implement NOT
        return (RSEF.ParsedAttributeCondition)root;
    }

    public static <S> RSEF.ParsedKeyCondition<S> createAST(RSEF.KeyCondition root) {
        return (RSEF.ParsedKeyCondition<S>) root;
    }
}
