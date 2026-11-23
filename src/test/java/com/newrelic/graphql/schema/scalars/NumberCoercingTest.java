/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import static com.newrelic.graphql.schema.scalars.CoercingTestSupport.CONTEXT;
import static com.newrelic.graphql.schema.scalars.CoercingTestSupport.LOCALE;
import static com.newrelic.graphql.schema.scalars.CoercingTestSupport.VARIABLES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

@ParameterizedClass(name = "{1}")
@MethodSource("data")
public class NumberCoercingTest {

  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {1580488444901L, new EpochMilliseconds(1580488444901L), EpochMilliseconds.getCoercing()},
          {
            1580488444901.234,
            new EpochMilliseconds(1580488444901.234),
            EpochMilliseconds.getCoercing()
          },
          {15800000, new EpochMilliseconds(15800000L), EpochMilliseconds.getCoercing()},
          {1580725125L, new EpochSeconds(1580725125L), EpochSeconds.getCoercing()},
          {1580725125, new EpochSeconds(1580725125L), EpochSeconds.getCoercing()},
          {1580725125.234, new EpochSeconds(1580725125.234), EpochSeconds.getCoercing()},
          {1000L, new Milliseconds(1000L), Milliseconds.getCoercing()},
          {1000, new Milliseconds(1000L), Milliseconds.getCoercing()},
          {1000.234, new Milliseconds(1000.234), Milliseconds.getCoercing()},
          {5000L, new Seconds(5000L), Seconds.getCoercing()},
          {5000, new Seconds(5000L), Seconds.getCoercing()},
          {5000.234, new Seconds(5000.234), Seconds.getCoercing()},
          {25L, new Minutes(25L), Minutes.getCoercing()},
          {25, new Minutes(25L), Minutes.getCoercing()},
          {25.25, new Minutes(25.25), Minutes.getCoercing()}
        });
  }

  private Number originalValue;
  private NumberCoercing.NumberWrapper numberWrapper;
  private NumberCoercing<?> coercing;

  public NumberCoercingTest(
      Number originalValue,
      NumberCoercing.NumberWrapper numberWrapper,
      NumberCoercing<?> coercing) {
    this.originalValue = originalValue;
    this.numberWrapper = numberWrapper;
    this.coercing = coercing;
  }

  @Test
  public void parseValue() {
    NumberCoercing.NumberWrapper actual = coercing.parseValue(originalValue, CONTEXT, LOCALE);
    assertEquals(numberWrapper, actual);
  }

  @Test
  public void parseValueInvalid() {
    assertThrows(
        CoercingParseValueException.class,
        () -> coercing.parseValue("111111111111111111111111111111", CONTEXT, LOCALE));
  }

  @Test
  public void parseValueNull() {
    assertThrows(
        CoercingParseValueException.class, () -> coercing.parseValue(null, CONTEXT, LOCALE));
  }

  @Test
  public void parseStringLiteral() {
    StringValue literal = new StringValue(originalValue.toString());
    assertEquals(coercing.parseLiteral(literal, VARIABLES, CONTEXT, LOCALE), numberWrapper);
  }

  @Test
  public void parseIntLiteral() {
    // This path is only valid if input is integral
    if (originalValue instanceof Integer || originalValue instanceof Long) {
      IntValue literal = new IntValue(BigInteger.valueOf(originalValue.longValue()));
      assertEquals(coercing.parseLiteral(literal, VARIABLES, CONTEXT, LOCALE), numberWrapper);
    }
  }

  @Test
  public void parseFloatLiteral() {
    // This path is only valid if input is floating point
    if (originalValue instanceof Float || originalValue instanceof Double) {
      FloatValue literal = new FloatValue(BigDecimal.valueOf(originalValue.doubleValue()));
      assertEquals(coercing.parseLiteral(literal, VARIABLES, CONTEXT, LOCALE), numberWrapper);
    }
  }

  @Test
  public void parseLiteralInvalid() {
    assertThrows(
        CoercingParseLiteralException.class,
        () -> {
          StringValue literal = new StringValue("not a number");
          coercing.parseLiteral(literal, VARIABLES, CONTEXT, LOCALE);
        });
  }

  @Test
  public void parseLiteralNull() {
    assertThrows(
        CoercingParseLiteralException.class,
        () -> coercing.parseLiteral(null, VARIABLES, CONTEXT, LOCALE));
  }

  @Test
  public void serialize() {
    Number actual = coercing.serialize(numberWrapper, CONTEXT, LOCALE);
    assertEquals(numberWrapper.getRawValue(), actual);
  }

  @Test
  public void serializeInvalidType() {
    assertThrows(
        CoercingSerializeException.class, () -> coercing.serialize(new Object(), CONTEXT, LOCALE));
  }

  @Test
  public void serializeNull() {
    assertThrows(CoercingSerializeException.class, () -> coercing.serialize(null, CONTEXT, LOCALE));
  }
}
