/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend;

import com.schibsted.security.strongbox.sdk.types.State;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;

import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.name;
import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.notAfter;
import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.notBefore;
import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.state;

/**
 * Experimentation with stream approach instead of the somewhat awkward existing filter.
 * A bit tricky to make this smooth as we don't have operator overloading in Java.
 *
 * @author stiankri
 */
public class SecretEventStream {
    void examples() {
        KVStream<RawSecretEntry> entryStream = new KVStream<>(new DummyExecutor());

        List<RawSecretEntry> entries = entryStream
                .filter(name.eq(new SecretIdentifier("toByteArray")))
                //.filter(notAfter.get().eq(notBefore.get()).and(state.eq(State.ENABLED)))
                .reverse()
                .uniquePrimaryKey()
                .toList();

        List<RawSecretEntry> entries2 = entryStream
                .filter(name.eq(new SecretIdentifier("toByteArray"))).toList();

        List<RawSecretEntry> entries3 = entryStream
                .filter(state.eq(state)
                        .AND(notBefore.isNotPresent().OR(notBefore.get().le(notBefore.get()))
                        .AND(notAfter.isNotPresent().OR(notAfter.get().ge(notAfter.get()))))).toList();

        Optional<RawSecretEntry> entries4 = entryStream
                .filter(name.eq(new SecretIdentifier("toByteArray")))
                .findFirst();

        List<RawSecretEntry> entries5 = entryStream
                .filter(notAfter.get().eq(notAfter.get()))
                .uniquePrimaryKey()
                .toList();

        entryStream
                .filter(RSEF.partitionKey.eq("toByteArray").AND(RSEF.sortKey.eq("1")))
                .filter(state.eq(State.ENABLED))
                .toList();

        entryStream
                .filter(RSEF.partitionKey.eq("toByteArray").AND(RSEF.sortKey.eq("1")))
                .filter(state.eq(State.ENABLED))
                .toList();
    }

    // This is either for DynamoDB, GenericFile, etc.
    public interface Executor<T> {
        Stream<T> toJavaStream(Filter<T> filter);
    }

    public static class DummyExecutor implements Executor<RawSecretEntry> {
        @Override
        public Stream<RawSecretEntry> toJavaStream(Filter<RawSecretEntry> filter) {
            return null;
        }
    }

    public static class Filter<S> {
        public Optional<RSEF.KeyCondition> keyCondition;
        public Optional<RSEF.AttributeCondition> attributeFilter;
        public Optional<RSEF.ParsedAttributeCondition<S>> parsedAttributeCondition;
        public Optional<RSEF.ParsedKeyCondition<S>> parsedKeyCondition;
        public boolean reverse;
        public boolean unique;

        public Filter(Optional<RSEF.KeyCondition> keyCondition, Optional<RSEF.ParsedKeyCondition<S>> parsedKeyCondition, Optional<RSEF.AttributeCondition> attributeFilter, Optional<RSEF.ParsedAttributeCondition<S>> parsedAttributeCondition, boolean reverse, boolean unique) {
            this.keyCondition = keyCondition;
            this.parsedKeyCondition = parsedKeyCondition;
            this.attributeFilter = attributeFilter;
            this.parsedAttributeCondition = parsedAttributeCondition;
            this.reverse = reverse;
            this.unique = unique;
        }
    }

    public static class EntryStreamUnique<S> {
        protected Optional<RSEF.KeyCondition> keyCondition = Optional.empty();
        protected Optional<RSEF.AttributeCondition> attributeCondition = Optional.empty();
        protected Optional<RSEF.ParsedAttributeCondition<S>> parsedAttributeCondition = Optional.empty();
        protected Optional<RSEF.ParsedKeyCondition<S>> parsedKeyCondition = Optional.empty();
        protected boolean reverse = false;
        protected boolean unique = false;

        private Executor<S> executor;

        public EntryStreamUnique(Executor<S> executor) {
            this.executor = executor;
        }

        public Stream<S> toJavaStream() {
            parse();
            return executor.toJavaStream(new Filter<S>(keyCondition, parsedKeyCondition, attributeCondition, parsedAttributeCondition, reverse, unique));
        }

        // TODO: implement collector instead? or only Set?
        public List<S> toList() {
            return toJavaStream().collect(Collectors.toList());
        }

        public Optional<S> findFirst() {
            return toJavaStream().findFirst();
        }

        public void forEach(Consumer<? super S> action) {
            toJavaStream().forEach(action);
        }

        private void parse() {
            if (keyCondition.isPresent()) {
                parsedKeyCondition = Optional.of(Parser.createAST(keyCondition.get()));
            }

            if (attributeCondition.isPresent()) {
                parsedAttributeCondition = Optional.of(Parser.createAST(attributeCondition.get()));
            }
        }
    }


    public static class EntryStreamReverse<T> extends EntryStreamUnique<T> {
        public EntryStreamReverse(Executor<T> executor) {
            super(executor);
        }

        public EntryStreamUnique<T> uniquePrimaryKey() {
            unique = true;
            return this;
        }
    }

    public static class EntryStreamAttribute<T> extends EntryStreamReverse<T> {
        public EntryStreamAttribute(Executor<T> executor) {
            super(executor);
        }

        public EntryStreamReverse<T> reverse() {
            reverse = true;
            return this;
        }
    }

    public static class EntryStreamKey<T> extends EntryStreamAttribute<T> {
        public EntryStreamKey(Executor<T> executor) {
            super(executor);
        }

        public EntryStreamAttribute<T> filter(RSEF.AttributeCondition condition) {
            attributeCondition = Optional.of(condition);
            return this;
        }
    }
}
