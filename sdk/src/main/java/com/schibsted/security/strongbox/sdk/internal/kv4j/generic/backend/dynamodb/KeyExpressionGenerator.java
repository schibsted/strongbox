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

package com.schibsted.security.strongbox.sdk.internal.kv4j.generic.backend.dynamodb;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.schibsted.security.strongbox.sdk.exceptions.UnsupportedTypeException;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend.RSEF;
import com.schibsted.security.strongbox.sdk.internal.converter.Converters;

import java.util.HashMap;
import java.util.Map;

/**
 * Translate the internal AST into a DynamoDB expression
 *
 * @author stiankri
 */
public class KeyExpressionGenerator {

    public int count = 0;

    public <Entry> KeyCondition process(RSEF.ParsedKeyCondition<Entry> keyCondition, Converters converters) {

        if (keyCondition instanceof RSEF.KeyAND) {
            RSEF.KeyAND<Entry> current = (RSEF.KeyAND<Entry>) keyCondition;

            KeyCondition left = process(current.left, converters);
            KeyCondition right = process(current.right, converters);

            Map<String, String> expressionAttributeNames= FilterGenerator.merge(left.expressionAttributeNames, right.expressionAttributeNames);
            Map<String, AttributeValue> expressionAttributeValues= FilterGenerator.merge(left.expressionAttributeValues, right.expressionAttributeValues);
            String keyConditionExpression = String.format("(%s) AND (%s)", left.keyConditionExpression, right.keyConditionExpression);

            return new KeyCondition(expressionAttributeNames, expressionAttributeValues, keyConditionExpression);
        } else if (keyCondition instanceof RSEF.PartitionKeyEqualityOperator) {
            RSEF.PartitionKeyEqualityOperator<Entry, ?> current = (RSEF.PartitionKeyEqualityOperator<Entry, ?>) keyCondition;

            KeyCondition left = createTerm(current.left, converters);
            KeyCondition right = createTerm(current.right, converters);

            Map<String, String> expressionAttributeNames= FilterGenerator.merge(left.expressionAttributeNames, right.expressionAttributeNames);
            Map<String, AttributeValue> expressionAttributeValues= FilterGenerator.merge(left.expressionAttributeValues, right.expressionAttributeValues);
            String keyConditionExpression = String.format("%s = %s", left.keyConditionExpression, right.keyConditionExpression);

            return new KeyCondition(expressionAttributeNames, expressionAttributeValues, keyConditionExpression);
        } else if (keyCondition instanceof RSEF.SortKeyComparisonOperator) {
            RSEF.SortKeyComparisonOperator<Entry, ?> current = (RSEF.SortKeyComparisonOperator<Entry, ?>) keyCondition;

            KeyCondition left = createTerm(current.left, converters);
            KeyCondition right = createTerm(current.right, converters);

            Map<String, String> expressionAttributeNames= FilterGenerator.merge(left.expressionAttributeNames, right.expressionAttributeNames);
            Map<String, AttributeValue> expressionAttributeValues= FilterGenerator.merge(left.expressionAttributeValues, right.expressionAttributeValues);
            String keyConditionExpression = String.format("%s %s %s", left.keyConditionExpression, FilterGenerator.getOperator(current.binaryOpType), right.keyConditionExpression);

            return new KeyCondition(expressionAttributeNames, expressionAttributeValues, keyConditionExpression);
        } else {
            throw new UnsupportedTypeException(keyCondition.getClass().getName());
        }
    }

    public <T> KeyCondition createTerm(RSEF.TypedTerm<T> typedTerm, Converters converters) {
        if (typedTerm instanceof RSEF.TypedPartitionKeyReference) {
            RSEF.TypedPartitionKeyReference<T> current = (RSEF.TypedPartitionKeyReference<T>) typedTerm;

            Map<String, String> expressionAttributeNames = new HashMap<>();
            String paddedName = String.format("#%s", current.position.toString());
            expressionAttributeNames.put(paddedName, current.position.toString());

            return new KeyCondition(expressionAttributeNames, paddedName);
        } else if (typedTerm instanceof RSEF.TypedSortKeyReference) {
            // TODO: cleanup/merge with the non-optional
            RSEF.TypedSortKeyReference<T> current = (RSEF.TypedSortKeyReference<T>) typedTerm;

            Map<String, String> expressionAttributeNames = new HashMap<>();
            String paddedName = String.format("#%s", current.position.toString());
            expressionAttributeNames.put(paddedName, current.position.toString());

            return new KeyCondition(expressionAttributeNames, paddedName);
        } else if (typedTerm instanceof  RSEF.TypedLiteral) {
            RSEF.TypedLiteral<T> current = (RSEF.TypedLiteral<T>) typedTerm;

            String placeHolderName = String.format(":%s", FilterGenerator.convertNumberToLetters(count++));
            String value = converters.to(current.value);
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            AttributeValue attributeValue = new AttributeValue();

            if (current.value instanceof Long) {
                attributeValue.setN(value);
            } else {
                attributeValue.setS(value);
            }
            expressionAttributeValues.put(placeHolderName, attributeValue);

            return new KeyCondition(placeHolderName, expressionAttributeValues);
        } else {
            throw new UnsupportedTypeException(typedTerm.getClass().getName());
        }
    }

    public static class KeyCondition {
        public final Map<String, String> expressionAttributeNames;
        public final Map<String, AttributeValue> expressionAttributeValues;
        public final String keyConditionExpression;

        public KeyCondition(Map<String, String> expressionAttributeNames, Map<String, AttributeValue> expressionAttributeValues, String keyConditionExpression) {
            this.expressionAttributeNames = expressionAttributeNames;
            this.expressionAttributeValues = expressionAttributeValues;
            this.keyConditionExpression = keyConditionExpression;
        }

        public KeyCondition(Map<String, String> expressionAttributeNames, String keyConditionExpression) {
            this.expressionAttributeNames = expressionAttributeNames;
            this.expressionAttributeValues = new HashMap<>();
            this.keyConditionExpression = keyConditionExpression;
        }

        public KeyCondition(String keyConditionExpression, Map<String, AttributeValue> expressionAttributeValues) {
            this.expressionAttributeNames = new HashMap<>();
            this.expressionAttributeValues = expressionAttributeValues;
            this.keyConditionExpression = keyConditionExpression;
        }
    }
}
