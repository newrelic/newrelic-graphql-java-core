/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Base implementation of the {@code graphql-java} {@code Coercing} interface to deal with varying
 * generic numeric types.
 *
 * @param <T> Destination numeric wrapping type to coerce for
 */
public abstract class NumberCoercing<T extends NumberCoercing.NumberWrapper>
    implements Coercing<T, Number> {

  /**
   * Called to convert a Java object result of a DataFetcher to a valid runtime value for the scalar
   * type.
   *
   * @param input Instance of type NumberWrapper
   * @param graphQLContext Context of the current execution
   * @param locale Locale of the current execution
   * @return Number conversion from wrapper type
   */
  @Override
  public Number serialize(Object input, GraphQLContext graphQLContext, Locale locale) {
    try {
      return ((NumberWrapper) input).value;
    } catch (Exception e) {
      throw new CoercingSerializeException(e.getMessage(), e);
    }
  }

  /**
   * Called to resolve a input from a query variable into a Java object acceptable for the scalar
   * type.
   *
   * @param input Query variable value
   * @param graphQLContext Context of the current execution
   * @param locale Locale of the current execution
   * @return Number converted to wrapper type
   */
  @Override
  public T parseValue(Object input, GraphQLContext graphQLContext, Locale locale) {
    if (input instanceof Number) {
      BigDecimal dec = NumberWrapper.toBigDecimal((Number) input);
      return fromBigDecimal(dec);
    }
    throw new CoercingParseValueException(String.format("'%s' is not a number", input));
  }

  /**
   * Called during query validation to convert an query input AST node into a Java object acceptable
   * for the scalar type.
   *
   * @param input AST node from query input
   * @param variables Coerced variables of the current execution
   * @param graphQLContext Context of the current execution
   * @param locale Locale of the current execution
   * @return Number converted to wrapper type
   */
  @Override
  public T parseLiteral(
      Value<?> input, CoercedVariables variables, GraphQLContext graphQLContext, Locale locale) {
    try {
      if (input instanceof StringValue) {
        String value = ((StringValue) input).getValue();
        return fromBigDecimal(new BigDecimal(value));
      } else if (input instanceof IntValue) {
        BigInteger value = ((IntValue) input).getValue();
        return fromNumber(value.longValue());
      } else if (input instanceof FloatValue) {
        BigDecimal value = ((FloatValue) input).getValue();
        return fromNumber(value.doubleValue());
      }
    } catch (Exception ex) {
      throw new CoercingParseLiteralException(ex.getMessage());
    }
    throw new CoercingParseLiteralException(String.format("'%s' is not a number", input));
  }

  protected T fromBigDecimal(BigDecimal dec) {
    if (dec.scale() <= 0) {
      return fromNumber(dec.longValueExact());
    } else {
      return fromNumber(dec.doubleValue());
    }
  }

  protected abstract T fromNumber(Number value);

  /** Base class for numeric wrapper types. */
  public abstract static class NumberWrapper {
    private Number value;

    /**
     * @param value Incoming numeric value
     */
    public NumberWrapper(Number value) {
      this.value = value;
    }

    /**
     * @return Raw value in the wrapper was constructed with
     */
    public Number getRawValue() {
      return value;
    }

    protected static BigDecimal toBigDecimal(Number n) {
      BigDecimal dec;
      if (n instanceof Integer || n instanceof Long) {
        dec = new BigDecimal(n.longValue());
      } else {
        dec = new BigDecimal(n.doubleValue());
      }
      return dec;
    }

    // Support method for deserialization from instances of our own type
    protected static Number rawValueFromProps(Map<String, Object> props) {
      Number value = (Number) props.get("rawValue");
      if (value == null) {
        throw new IllegalArgumentException("Can't deserialize from properties missing 'rawValue'");
      }

      return value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      NumberWrapper that = (NumberWrapper) o;
      return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value);
    }
  }
}
