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
