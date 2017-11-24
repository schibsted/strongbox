/*
 * Copyright 2017 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */
package com.schibsted.security.strongbox.springboot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Value("")
public @interface StrongboxValue {

  @AliasFor(annotation = Value.class, attribute = "value")
  String value();

}
