/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend;

import com.schibsted.security.strongbox.sdk.exceptions.FieldAccessException;
import com.schibsted.security.strongbox.sdk.exceptions.NoFieldWithPositionException;
import com.schibsted.security.strongbox.sdk.exceptions.UnsupportedTypeException;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * A LINQ inspired query language compatible with AWS DynamoDB's query language.
 *
 * Please note: AWS' has updated their documentation to not include the information needed anymore.
 * This does not bode well for future compatibility.
 *
 * Original location of the docs:
 * https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.SpecifyingConditions.html#ConditionExpressionReference
 *
 * Old version of the docs this work is based on:
 * https://web.archive.org/web/20160414033837/https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.SpecifyingConditions.html
 *
 * Syntax:
 *  condition-expression ::=
 *     operand comparator operand
 *    | operand BETWEEN operand AND operand
 *    | operand IN ( operand (',' operand (, ...) ))
 *    | function
 *    | condition AND condition
 *    | condition OR condition
 *    | NOT condition
 *    | ( condition )
 *
 *  comparator ::=
 *    =
 *    | <>
 *    | <
 *    | <=
 *    | >
 *    | >=
 *
 *  function ::=
 *      attribute_exists (path)
 *    | attribute_not_exists (path)
 *    | attribute_type (path, type)
 *    | begins_with (path, substr)
 *    | contains (path, operand)
 *    | size (path)
 *
 * Precedence:
 *  = <> < <= > >=
 *  IN
 *  BETWEEN
 *  attribute_exists attribute_not_exists begins_with contains
 *  Parentheses
 *  NOT
 *  AND
 *  OR
 *
 * @author stiankri
 *
 */
public class RSEF {
    public static PartitionKey<RawSecretEntry, String> partitionKey = new PartitionKey<>(Config.NOT_BEFORE);
    public static SortKey<RawSecretEntry, String> sortKey = new SortKey<>(Config.NOT_AFTER);

    public abstract static class AttributeCondition {
        public AttributeCondition OR(AttributeCondition other) {
            CompositeAttributeCondition composite = new CompositeAttributeCondition(this);
            return composite.OR(other);
        }

        public AttributeCondition AND(AttributeCondition other) {
            CompositeAttributeCondition composite = new CompositeAttributeCondition(this);
            return composite.AND(other);
        }
    }

    public static <T> AttributeCondition NOT(AttributeCondition condition) {
        return new NotOperator<T>((ParsedAttributeCondition<T>)condition);
    }

    public static class CompositeAttributeCondition extends AttributeCondition {
        public LinkedList<AttributeCondition> conditionals = new LinkedList<>();
        public LinkedList<LogicalOperatorToken> logicalOperatorTokens = new LinkedList<>();

        CompositeAttributeCondition(AttributeCondition c) {
            conditionals.add(c);
        }

        @Override
        public AttributeCondition OR(AttributeCondition other) {
            conditionals.add(other);
            logicalOperatorTokens.add(LogicalOperatorToken.OR);

            return this;
        }

        @Override
        public AttributeCondition AND(AttributeCondition other) {
            conditionals.add(other);
            logicalOperatorTokens.add(LogicalOperatorToken.AND);

            return this;
        }

    }

    public enum LogicalOperatorToken {
        OR, AND;
    }

    public abstract static class SortKeyCondition {
    }

    public abstract static class KeyCondition<S> {
    }

    public static class KeyAND<S> extends KeyCondition implements ParsedKeyCondition<S> {
        public PartitionKeyEqualityOperator<S, ?> left;
        public SortKeyComparisonOperator<S, ?> right;

        public KeyAND(PartitionKeyEqualityOperator<S, ?> left, SortKeyComparisonOperator<S, ?> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean evaluate(S entry) {
            return left.evaluate(entry) & right.evaluate(entry);
        }
    }

    public static class SortKeyComparisonOperator<S, T extends Comparable<? super T>> extends SortKeyCondition implements ParsedKeyCondition<S> {
        public TypedTerm<T> left;
        public TypedTerm<T> right;
        public BinaryOpType binaryOpType;

        public SortKeyComparisonOperator(TypedTerm<T> left, TypedTerm<T> right, BinaryOpType binaryOpType) {
            this.left = left;
            this.right = right;
            this.binaryOpType = binaryOpType;
        }

        @Override
        public boolean evaluate(S entry) {
            T l = extract(left, entry);
            T r = extract(right, entry);

            return compareTo(l, r, binaryOpType);
        }
    }

    public static class PartitionKeyEqualityOperator<S, T> extends KeyCondition implements ParsedKeyCondition<S> {
        public TypedPartitionKeyReference<T> left;
        public TypedLiteral<T> right;

        public PartitionKeyEqualityOperator(TypedPartitionKeyReference<T> left, TypedLiteral<T> right) {
            this.left = left;
            this.right = right;
        }

        public KeyCondition AND(SortKeyComparisonOperator<S, ?> other) {
            return new KeyAND<>(this, other);
        }

        @Override
        public boolean evaluate(S entry) {
            T l = extract(left, entry);
            T r = extract(right, entry);

            return l.equals(r);
        }
    }

    public abstract static class Term {
    }

    public enum BinaryOpType {
        EQ, GE, GT, LE, LT, NE;
    }

    public static class NOT extends AttributeCondition {
        public AttributeCondition condition;

        public NOT(AttributeCondition condition) {
            this.condition = condition;
        }
    }

    public abstract static class TypedTerm<T> {

    }

    public static class TypedLiteral<T> extends TypedTerm<T> {
        public final T value;

        public TypedLiteral(T value) {
            this.value = value;
        }
    }

    public static class TypedAttributeReference<T> extends TypedTerm<T> {
        public final Integer position;

        public TypedAttributeReference(int position) {
            this.position = position;
        }
    }

    public static class TypedSortKeyReference<T> extends TypedTerm<T> {
        public final Integer position;

        public TypedSortKeyReference(int position) {
            this.position = position;
        }
    }

    public static class TypedPartitionKeyReference<T> extends TypedTerm<T> {
        public final Integer position;

        public TypedPartitionKeyReference(int position) {
            this.position = position;
        }
    }

    public static class TypedOptionalAttributeReference<T> extends TypedTerm<T> {
        public final Integer position;

        public TypedOptionalAttributeReference(int position) {
            this.position = position;
        }
    }

    public interface ParsedAttributeCondition<S> {
        boolean evaluate(S o);
    }

    public interface ParsedKeyCondition<S> {
        boolean evaluate(S o);
    }

    public static class AndOperator<S> extends AttributeCondition implements ParsedAttributeCondition<S> {
        public final ParsedAttributeCondition<S> left;
        public final ParsedAttributeCondition<S> right;

        public AndOperator(ParsedAttributeCondition<S> right, ParsedAttributeCondition<S> left) {
            this.right = right;
            this.left = left;
        }

        @Override
        public boolean evaluate(S entry) {
            return left.evaluate(entry) & right.evaluate(entry);
        }
    }

    public static class OrOperator<S> extends AttributeCondition implements ParsedAttributeCondition<S> {
        public final ParsedAttributeCondition<S> left;
        public final ParsedAttributeCondition<S> right;

        public OrOperator(ParsedAttributeCondition<S> left, ParsedAttributeCondition<S> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean evaluate(S entry) {
            return left.evaluate(entry) | right.evaluate(entry);
        }
    }

    public static class NotOperator<S> extends AttributeCondition implements ParsedAttributeCondition<S> {
        public final ParsedAttributeCondition<S> left;

        public NotOperator(ParsedAttributeCondition<S> left) {
            this.left = left;
        }

        @Override
        public boolean evaluate(S entry) {
            return !left.evaluate(entry);
        }
    }


    public static class ExistsOperator<S, T> extends AttributeCondition implements ParsedAttributeCondition<S> {
        public final TypedOptionalAttributeReference<T> reference;

        public ExistsOperator(TypedOptionalAttributeReference<T> reference) {
            this.reference = reference;
        }

        @Override
        public boolean evaluate(S entry) {
            return exists(reference, entry);
        }
    }

    public static class NotExistsOperator<S, T> extends AttributeCondition implements ParsedAttributeCondition<S> {
        public final TypedOptionalAttributeReference<T> reference;

        public NotExistsOperator(TypedOptionalAttributeReference<T> reference) {
            this.reference = reference;
        }

        @Override
        public boolean evaluate(S entry) {
            return !exists(reference, entry);
        }
    }

    private static <S, T> boolean exists(TypedOptionalAttributeReference<T> ref, S entry) {
        String fieldName = getFieldName(entry, ref.position);
        try {
            Optional<T> value = (Optional<T>) entry.getClass().getField(fieldName).get(entry);
            return value.isPresent();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new FieldAccessException(fieldName, entry .getClass().getName(), e);
        }
    }

    public static class ComparisonOperator<S, T extends Comparable<? super T>> extends AttributeCondition implements ParsedAttributeCondition<S> {
        public TypedTerm<T> left;
        public TypedTerm<T> right;
        public BinaryOpType binaryOpType;

        ComparisonOperator(TypedTerm<T> left, TypedTerm<T> right, BinaryOpType binaryOpType) {
            this.left = left;
            this.right = right;
            this.binaryOpType = binaryOpType;
        }

        @Override
        public boolean evaluate(S entry) {
            T l = extract(left, entry);
            T r = extract(right, entry);

            return compareTo(l, r, binaryOpType);
        }
    }

    private static <T extends Comparable<? super T>> boolean compareTo(T l, T r, BinaryOpType binaryOpType) {
        if (l != null && r != null) {
            switch (binaryOpType) {
                case EQ:
                    return l.compareTo(r) == 0;
                case GE:
                    return l.compareTo(r) >= 0;
                case GT:
                    return l.compareTo(r) > 0;
                case LE:
                    return l.compareTo(r) <= 0;
                case LT:
                    return l.compareTo(r) < 0;
                case NE:
                    return l.compareTo(r) != 0;
                default:
                    throw new UnsupportedTypeException(binaryOpType.name());
            }
        } else {
            return false;
        }
    }

    static private Map<Class, Map<Integer, String>> map = new HashMap<>();
    private static <S> String getFieldName(S entry, int annotationPosition) {
        Map<Integer, String> classMappings = map.get(entry.getClass());
        if (classMappings == null) {
            // scan and store
            classMappings = new HashMap<>();

            Field[] fields = entry.getClass().getDeclaredFields();
            for (Field field : fields) {
                com.schibsted.security.strongbox.sdk.internal.kv4j.generic.annotation.Attribute[] attributes = field.getAnnotationsByType(com.schibsted.security.strongbox.sdk.internal.kv4j.generic.annotation.Attribute.class);
                com.schibsted.security.strongbox.sdk.internal.kv4j.generic.annotation.PartitionKey[] partitionKey = field.getAnnotationsByType(com.schibsted.security.strongbox.sdk.internal.kv4j.generic.annotation.PartitionKey.class);
                com.schibsted.security.strongbox.sdk.internal.kv4j.generic.annotation.SortKey[] sortKey = field.getAnnotationsByType(com.schibsted.security.strongbox.sdk.internal.kv4j.generic.annotation.SortKey.class);

                if (attributes.length > 0) {
                    int position = attributes[0].position();
                    classMappings.put(position, field.getName());
                }

                if (partitionKey.length > 0) {
                    int position = partitionKey[0].position();
                    classMappings.put(position, field.getName());
                }

                if (sortKey.length > 0) {
                    int position = sortKey[0].position();
                    classMappings.put(position, field.getName());
                }
            }

            map.put(entry.getClass(), classMappings);
        }

        String fieldName = classMappings.get(annotationPosition);
        if (fieldName != null) {
            return fieldName;
        } else {
            throw new NoFieldWithPositionException(annotationPosition, entry.getClass().getName());
        }
    }

    private static <S, T> T extract(TypedTerm<T> term, S t) {
        if (term instanceof TypedLiteral) {
            return ((TypedLiteral<T>) term).value;
        }

        String fieldName = "";
        try {
            if (term instanceof TypedAttributeReference) {
                TypedAttributeReference<T> ref = (TypedAttributeReference<T>) term;
                fieldName = getFieldName(t, ref.position);
                return (T) t.getClass().getField(fieldName).get(t);
            } else if (term instanceof TypedPartitionKeyReference) {
                TypedPartitionKeyReference<T> ref = (TypedPartitionKeyReference<T>) term;
                fieldName = getFieldName(t, ref.position);
                return (T) t.getClass().getField(fieldName).get(t);
            } else if (term instanceof TypedSortKeyReference) {
                TypedSortKeyReference<T> ref = (TypedSortKeyReference<T>) term;
                fieldName = getFieldName(t, ref.position);
                return (T) t.getClass().getField(fieldName).get(t);
            } else if (term instanceof TypedOptionalAttributeReference) {
                TypedOptionalAttributeReference<T> ref = (TypedOptionalAttributeReference<T>) term;
                fieldName = getFieldName(t, ref.position);
                Optional<T> v = (Optional<T>) t.getClass().getField(fieldName).get(t);
                return v.isPresent() ? v.get() : null;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new FieldAccessException(fieldName, t.getClass().getName(), e);
        }

        throw new UnsupportedTypeException(term.getClass().getName());
    }

    // https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Query.html#DDB-Query-request-KeyConditionExpression
    // partition equality test is required, then the sort key can be used in an expression like the attributes
    // hard to make sure (statically) that there is exactly one PartitionKey EQ check
    public static class PartitionKey<S, T> {
        public TypedPartitionKeyReference<T> reference;

        public PartitionKey(int position) {
            this.reference = new TypedPartitionKeyReference<T>(position);
        }

        public PartitionKeyEqualityOperator<S, T> eq(T other) {
            return new PartitionKeyEqualityOperator<>(reference, new TypedLiteral<T>(other));
        }
    }

    public static class SortKey<S, T extends Comparable<? super T>> {
        public TypedSortKeyReference<T> reference;

        public SortKey(int position) {
            this.reference = new TypedSortKeyReference<>(position);
        }

        public SortKeyComparisonOperator<S, T> eq(T other) {
            return new SortKeyComparisonOperator<>(reference, new TypedLiteral<>(other), BinaryOpType.EQ);
        }

        public SortKeyComparisonOperator<S, T> ge(T other) {
            return new SortKeyComparisonOperator<>(reference, new TypedLiteral<>(other), BinaryOpType.GE);
        }

        public SortKeyComparisonOperator<S, T> gt(T other) {
            return new SortKeyComparisonOperator<>(reference, new TypedLiteral<>(other), BinaryOpType.GT);
        }

        public SortKeyComparisonOperator<S, T> le(T other) {
            return new SortKeyComparisonOperator<>(reference, new TypedLiteral<>(other), BinaryOpType.LE);
        }

        public SortKeyComparisonOperator<S, T> lt(T other) {
            return new SortKeyComparisonOperator<>(reference, new TypedLiteral<>(other), BinaryOpType.LT);
        }

        // TODO: between, begins_with
    }

    public static class Attribute<S, T extends Comparable<? super T>> {
        public TypedTerm<T> reference;

        public Attribute(int position) {
            this.reference = new TypedAttributeReference<T>(position);
        }

        public Attribute(TypedTerm<T> term) {
            this.reference = term;
        }

        public AttributeCondition eq(Attribute<S, T > other) {
            return new ComparisonOperator<S, T>(reference, other.reference, BinaryOpType.EQ);
        }
        public AttributeCondition eq(T other) {
            return new ComparisonOperator<>(reference, new TypedLiteral<T>(other), BinaryOpType.EQ);
        }

        public AttributeCondition ge(Attribute<S,T> other) {
            return new ComparisonOperator<S, T>(reference, other.reference, BinaryOpType.GE);
        }
        public AttributeCondition ge(T other) {
            return new ComparisonOperator<>(reference, new TypedLiteral<T>(other), BinaryOpType.GE);
        }

        public AttributeCondition gt(Attribute<S,T> other) {
            return new ComparisonOperator<S, T>(reference, other.reference, BinaryOpType.GT);
        }
        public AttributeCondition gt(T other) {
            return new ComparisonOperator<>(reference, new TypedLiteral<T>(other), BinaryOpType.GT);
        }

        public AttributeCondition le(Attribute<S,T> other) {
            return new ComparisonOperator<S, T>(reference, other.reference, BinaryOpType.LE);
        }
        public AttributeCondition le(T other) {
            return new ComparisonOperator<>(reference, new TypedLiteral<T>(other), BinaryOpType.LE);
        }

        public AttributeCondition lt(Attribute<S, T> other) {
            return new ComparisonOperator<S, T>(reference, other.reference, BinaryOpType.LT);
        }
        public AttributeCondition lt(T other) {
            return new ComparisonOperator<>(reference, new TypedLiteral<T>(other), BinaryOpType.LT);
        }

        public AttributeCondition ne(Attribute<S,T> other) {
            return new ComparisonOperator<S, T>(reference, other.reference, BinaryOpType.NE);
        }

        public void between(T lower, T upper) {
            throw new UnsupportedOperationException("Method not implemented");
        }

        public void in(T... args) {
            throw new UnsupportedOperationException("Method not implemented");
        }

        // "True if the attribute specified by path begins with a particular substring."
        // Comment: does this only work for the string type?
        public AttributeCondition begins_with(String substring) {
            throw new UnsupportedOperationException("Method not implemented");
        }

        // "True if the attribute at the specified path is of a particular data type"
        // Comment: should not need this as the fields are statically typed
        public AttributeCondition attribute_type(String stringTypeEnum) {
            throw new UnsupportedOperationException("Method not implemented");
        }

        // For string and set
        public AttributeCondition contains(String stringTypeEnum) {
            throw new UnsupportedOperationException("Method not implemented");
        }

        // Returns the size of the attribute
        // Comment: could be used with binary search to get the size as reported by DynamoDB
        public void size() {
            throw new UnsupportedOperationException("Method not implemented");
        }
    }

    public static class OptionalAttribute<S, T extends Comparable<? super T>> {
        public TypedOptionalAttributeReference<T> variable;

        public OptionalAttribute(int position) {
            this.variable = new TypedOptionalAttributeReference<T>(position);
        }

        public Attribute<S,T> get() {
            return new Attribute<>(variable);
        }

        public AttributeCondition isPresent() {
            return new ExistsOperator<S, T>(variable);
        }

        public AttributeCondition isNotPresent() {
            return new NotExistsOperator<S, T>(variable);
        }
    }
}
