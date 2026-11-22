/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import java.util.Locale;

/** Base class for implementations of {@code Coercing} interface that expect String literals */
public abstract class StringCoercing<T> implements Coercing<T, String> {

  /**
   * Called during query validation to convert an query input AST node into a Java object acceptable
   * for the scalar type.
   *
   * @param input AST node from query input
   * @param variables Coerced variables of the current execution
   * @param graphQLContext Context of the current execution
   * @param locale Locale of the current execution
   * @return String value from input
   * @throws CoercingParseLiteralException when AST input is not of expected type StringValue
   */
  @Override
  public T parseLiteral(
      Value<?> input, CoercedVariables variables, GraphQLContext graphQLContext, Locale locale)
      throws CoercingParseLiteralException {
    if (input == null) {
      throw new CoercingParseLiteralException("Expected AST type 'StringValue' but was null.");
    }

    if (!(input instanceof StringValue)) {
      throw new CoercingParseLiteralException(
          String.format(
              "Expected AST type 'StringValue' but was '%s'.", input.getClass().getSimpleName()));
    }

    try {
      return parseFromString(((StringValue) input).getValue());
    } catch (Exception e) {
      throw new CoercingParseLiteralException(e.getMessage(), e);
    }
  }

  /**
   * Required method that derived classes implement to get deserialization from strings.
   *
   * @param input String value
   * @return Coerced object of the desired type
   */
  protected abstract T parseFromString(String input);

  /**
   * Called to resolve a input from a query variable into a Java object acceptable for the scalar
   * type.
   *
   * @param input Query variable value
   * @param graphQLContext Context of the current execution
   * @param locale Locale of the current execution
   * @return String from input object
   */
  @Override
  public T parseValue(Object input, GraphQLContext graphQLContext, Locale locale)
      throws CoercingParseValueException {
    try {
      return parseFromString(input.toString());
    } catch (Exception e) {
      throw new CoercingParseValueException(e.getMessage(), e);
    }
  }

  /**
   * Called to convert a Java object result of a DataFetcher to a String for responding.
   *
   * @param input Instance of wrapped type
   * @param graphQLContext Context of the current execution
   * @param locale Locale of the current execution
   * @return String conversion from wrapper type
   */
  @Override
  public String serialize(Object input, GraphQLContext graphQLContext, Locale locale)
      throws CoercingSerializeException {
    try {
      return serializeToString(cast(input));
    } catch (Exception e) {
      throw new CoercingSerializeException(e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T cast(Object rawValue) {
    return (T) rawValue;
  }

  /**
   * Required method that derived classes implement to get serialization to strings.
   *
   * @param input Object of our desired type
   * @return String serialization of the object
   */
  protected abstract String serializeToString(T input);
}
