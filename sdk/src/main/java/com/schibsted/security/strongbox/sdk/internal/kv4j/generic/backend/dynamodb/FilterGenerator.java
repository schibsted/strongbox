/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
public class FilterGenerator {
    public int count;

    FilterGenerator(int startCount) {
        this.count = startCount;
    }

    FilterGenerator() {
        this.count = 0;
    }

    public <Entry> Filter process(RSEF.ParsedAttributeCondition<Entry> attributeCondition, Converters converters) {

        if (attributeCondition instanceof RSEF.AndOperator) {
            RSEF.AndOperator<Entry> current = (RSEF.AndOperator<Entry>) attributeCondition;

            Filter left = process(current.left, converters);
            Filter right = process(current.right, converters);

            Map<String, String> expressionAttributeNames = merge(left.expressionAttributeNames, right.expressionAttributeNames);
            Map<String, AttributeValue> expressionAttributeValues = merge(left.expressionAttributeValues, right.expressionAttributeValues);
            String filterExpression = String.format("(%s) AND (%s)", left.filterExpression, right.filterExpression);

            return new Filter(expressionAttributeNames, expressionAttributeValues, filterExpression);
        } else if (attributeCondition instanceof RSEF.OrOperator) {
            RSEF.OrOperator<Entry> current = (RSEF.OrOperator<Entry>) attributeCondition;

            Filter left = process(current.left, converters);
            Filter right = process(current.right, converters);

            Map<String, String> expressionAttributeNames = merge(left.expressionAttributeNames, right.expressionAttributeNames);
            Map<String, AttributeValue> expressionAttributeValues = merge(left.expressionAttributeValues, right.expressionAttributeValues);
            String filterExpression = String.format("(%s) OR (%s)", left.filterExpression, right.filterExpression);

            return new Filter(expressionAttributeNames, expressionAttributeValues, filterExpression);
        } else if (attributeCondition instanceof RSEF.NotOperator) {
            RSEF.NotOperator<Entry> current = (RSEF.NotOperator<Entry>) attributeCondition;

            Filter left = process(current.left, converters);
            String filterExpression = String.format("NOT (%s)", left.filterExpression);

            return new Filter(left.expressionAttributeNames, left.expressionAttributeValues, filterExpression);
        } else if (attributeCondition instanceof RSEF.ComparisonOperator) {
            RSEF.ComparisonOperator<Entry, ?> current = (RSEF.ComparisonOperator<Entry, ?>) attributeCondition;

            Filter left = createTerm(current.left, converters);
            Filter right = createTerm(current.right, converters);

            Map<String, String> expressionAttributeNames = merge(left.expressionAttributeNames, right.expressionAttributeNames);
            Map<String, AttributeValue> expressionAttributeValues = merge(left.expressionAttributeValues, right.expressionAttributeValues);
            String filterExpression = String.format("%s %s %s", left.filterExpression, getOperator(current.binaryOpType), right.filterExpression);

            return new Filter(expressionAttributeNames, expressionAttributeValues, filterExpression);
        } else if (attributeCondition instanceof RSEF.ExistsOperator) {
            RSEF.ExistsOperator<Entry, ?> current = (RSEF.ExistsOperator<Entry, ?>) attributeCondition;

            Filter left = createTerm(current.reference, converters);
            String filterExpression = String.format("attribute_exists(%s)", left.filterExpression);

            return new Filter(left.expressionAttributeNames, filterExpression);
        } else if (attributeCondition instanceof RSEF.NotExistsOperator) {
            RSEF.NotExistsOperator<Entry, ?> current = (RSEF.NotExistsOperator<Entry, ?>) attributeCondition;

            Filter left = createTerm(current.reference, converters);
            String filterExpression = String.format("attribute_not_exists(%s)", left.filterExpression);

            return new Filter(left.expressionAttributeNames, filterExpression);
        } else {
            throw new UnsupportedTypeException(attributeCondition.getClass().getName());
        }
    }

    public <T> Filter createTerm(RSEF.TypedTerm<T> typedTerm, Converters converters) {
        if (typedTerm instanceof RSEF.TypedAttributeReference) {
            RSEF.TypedAttributeReference<T> current = (RSEF.TypedAttributeReference<T>) typedTerm;

            Map<String, String> expressionAttributeNames = new HashMap<>();
            String paddedName = String.format("#%s", current.position.toString());
            expressionAttributeNames.put(paddedName, current.position.toString());

            return new Filter(expressionAttributeNames, paddedName);
        } else if (typedTerm instanceof RSEF.TypedOptionalAttributeReference) {
            // TODO: cleanup/merge with the non-optional
            RSEF.TypedOptionalAttributeReference<T> current = (RSEF.TypedOptionalAttributeReference<T>) typedTerm;

            Map<String, String> expressionAttributeNames = new HashMap<>();
            String paddedName = String.format("#%s", current.position.toString());
            expressionAttributeNames.put(paddedName, current.position.toString());

            return new Filter(expressionAttributeNames, paddedName);
        } else if (typedTerm instanceof  RSEF.TypedLiteral) {
            RSEF.TypedLiteral<T> current = (RSEF.TypedLiteral<T>) typedTerm;

            String placeHolderName = String.format(":%s", convertNumberToLetters(count++));
            Object value = converters.toObject(current.value);
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            AttributeValue attributeValue = new AttributeValue();

            // FIXME do not hardcode
            if (value instanceof Byte || value instanceof Long) {
                attributeValue.setN(value.toString());
            } else {
                attributeValue.setS((String)value);
            }
            expressionAttributeValues.put(placeHolderName, attributeValue);

            return new Filter(placeHolderName, expressionAttributeValues);
        } else {
            throw new UnsupportedTypeException(typedTerm.getClass().getName());
        }
    }

    public static String getOperator(RSEF.BinaryOpType binaryOpType) {
        switch (binaryOpType) {
            case EQ:
                return "=";
            case GE:
                return ">=";
            case GT:
                return ">";
            case LE:
                return "<=";
            case LT:
                return "<";
            case NE:
                return "<>";
            default:
                throw new UnsupportedTypeException(binaryOpType.name());
        }
    }

    public static <A, B> Map<A, B> merge(Map<A, B> left, Map<A, B> right) {
        Map<A, B> result = new HashMap<>();
        result.putAll(left);
        result.putAll(right);
        return result;
    }

    public static class Filter {
        public final Map<String, String> expressionAttributeNames;
        public final Map<String, AttributeValue> expressionAttributeValues;
        public final String filterExpression;

        public Filter(Map<String, String> expressionAttributeNames, Map<String, AttributeValue> expressionAttributeValues, String filterExpression) {
            this.expressionAttributeNames = expressionAttributeNames;
            this.expressionAttributeValues = expressionAttributeValues;
            this.filterExpression = filterExpression;
        }

        public Filter(Map<String, String> expressionAttributeNames, String filterExpression) {
            this.expressionAttributeNames = expressionAttributeNames;
            this.expressionAttributeValues = new HashMap<>();
            this.filterExpression = filterExpression;
        }

        public Filter(String filterExpression, Map<String, AttributeValue> expressionAttributeValues) {
            this.expressionAttributeNames = new HashMap<>();
            this.expressionAttributeValues = expressionAttributeValues;
            this.filterExpression = filterExpression;
        }
    }

    public static String convertNumberToLetters(int number) {
        int range = 'z'-'a' + 1;

        StringBuilder sb = new StringBuilder();
        do {
            int last = number % range;
            sb.append(Character.toChars('a' + last));
            number /= range;
        } while (number != 0);

        return sb.reverse().toString();
    }
}
