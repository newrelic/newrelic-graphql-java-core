/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import static com.newrelic.graphql.schema.scalars.CoercingTestSupport.CONTEXT;
import static com.newrelic.graphql.schema.scalars.CoercingTestSupport.LOCALE;
import static com.newrelic.graphql.schema.scalars.CoercingTestSupport.VARIABLES;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.Test;

public class DateTimeCoercingTest {

  private static final String zonedString = "1978-09-01T10:15:30-09:00";
  private static final ZonedDateTime zonedExample = ZonedDateTime.parse(zonedString);

  @Test
  public void parseValue() {
    DateTime expected = new DateTime(zonedExample);
    DateTime actual = new DateTimeCoercing().parseValue(zonedString, CONTEXT, LOCALE);
    assertEquals(expected, actual);
  }

  @Test
  public void parseValueInvalid() {
    CoercingParseValueException thrown =
        assertThrows(
            CoercingParseValueException.class,
            () -> new DateTimeCoercing().parseValue("not a date", CONTEXT, LOCALE));
    assertThat(thrown.getCause(), instanceOf(DateTimeParseException.class));
  }

  @Test
  public void parseValueNull() {
    CoercingParseValueException thrown =
        assertThrows(
            CoercingParseValueException.class,
            () -> new DateTimeCoercing().parseValue(null, CONTEXT, LOCALE));
    assertThat(thrown.getCause(), instanceOf(NullPointerException.class));
  }

  @Test
  public void parseLiteral() {
    StringValue literal = new StringValue(zonedString);
    DateTime actual = new DateTimeCoercing().parseLiteral(literal, VARIABLES, CONTEXT, LOCALE);
    assertEquals(new DateTime(zonedExample), actual);
  }

  @Test
  public void parseLiteralInvalidDate() {
    CoercingParseLiteralException thrown =
        assertThrows(
            CoercingParseLiteralException.class,
            () -> {
              StringValue literal = new StringValue("not a date");
              new DateTimeCoercing().parseLiteral(literal, VARIABLES, CONTEXT, LOCALE);
            });
    assertThat(thrown.getCause(), instanceOf(DateTimeParseException.class));
  }

  @Test
  public void parseLiteralFailsIfNotStringValue() {
    CoercingParseLiteralException thrown =
        assertThrows(
            CoercingParseLiteralException.class,
            () ->
                new DateTimeCoercing()
                    .parseLiteral(new IntValue(BigInteger.ONE), VARIABLES, CONTEXT, LOCALE));

    assertThat(thrown.getMessage(), containsString("IntValue"));
    assertThat(thrown.getMessage(), containsString("StringValue"));
  }

  @Test
  public void parseLiteralNull() {
    assertThrows(
        CoercingParseLiteralException.class,
        () -> new DateTimeCoercing().parseLiteral(null, VARIABLES, CONTEXT, LOCALE));
  }

  @Test
  public void serialize() {
    String actual = new DateTimeCoercing().serialize(new DateTime(zonedExample), CONTEXT, LOCALE);
    assertEquals(zonedString, actual);
  }

  @Test
  public void serializeInvalidType() {
    CoercingSerializeException thrown =
        assertThrows(
            CoercingSerializeException.class,
            () -> new DateTimeCoercing().serialize(new Object(), CONTEXT, LOCALE));
    assertThat(thrown.getCause(), instanceOf(ClassCastException.class));
  }

  @Test
  public void serializeNull() {
    CoercingSerializeException thrown =
        assertThrows(
            CoercingSerializeException.class,
            () -> new DateTimeCoercing().serialize(null, CONTEXT, LOCALE));
    assertThat(thrown.getCause(), instanceOf(NullPointerException.class));
  }
}
