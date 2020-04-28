/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import java.math.BigDecimal;
import java.math.BigInteger;
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
   * @return Number conversion from wrapper type
   */
  @Override
  public Number serialize(Object input) {
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
   * @return Number converted to wrapper type
   */
  @Override
  public T parseValue(Object input) {
    if (input instanceof Number) {
      BigDecimal dec = NumberWrapper.toBigDecimal((Number) input);
      return fromBigDecimal(dec);
    }
    throw new CoercingParseValueException(String.format("'%s' is not a number", input));
  }

  /**
   * Called during query validation to convert an query input AST node into a Java object acceptable
   * for the scalar type. The input object will be an instance of {@link graphql.language.Value}.
   *
   * @param input AST node from query input
   * @return Number converted to wrapper type
   */
  @Override
  public T parseLiteral(Object input) {
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

    /** @param value Incoming numeric value */
    public NumberWrapper(Number value) {
      this.value = value;
    }

    /** @return Raw value in the wrapper was constructed with */
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
