/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import org.junit.Test;

public class DateTimeCoercingTest {

  private static final String zonedString = "1978-09-01T10:15:30-09:00";
  private static final ZonedDateTime zonedExample = ZonedDateTime.parse(zonedString);

  @Test
  public void parseValue() {
    DateTime expected = new DateTime(zonedExample);
    DateTime actual = new DateTimeCoercing().parseValue(zonedString);
    assertEquals(expected, actual);
  }

  @Test
  public void parseValueInvalid() {
    CoercingParseValueException thrown =
        assertThrows(
            CoercingParseValueException.class,
            () -> new DateTimeCoercing().parseValue("not a date"));
    assertThat(thrown.getCause(), instanceOf(DateTimeParseException.class));
  }

  @Test
  public void parseValueNull() {
    CoercingParseValueException thrown =
        assertThrows(
            CoercingParseValueException.class, () -> new DateTimeCoercing().parseValue(null));
    assertThat(thrown.getCause(), instanceOf(NullPointerException.class));
  }

  @Test
  public void parseLiteral() {
    StringValue literal = new StringValue(zonedString);
    DateTime actual = new DateTimeCoercing().parseLiteral(literal);
    assertEquals(new DateTime(zonedExample), actual);
  }

  @Test
  public void parseLiteralInvalidGuid() {
    CoercingParseLiteralException thrown =
        assertThrows(
            CoercingParseLiteralException.class,
            () -> {
              StringValue literal = new StringValue("not a date");
              new DateTimeCoercing().parseLiteral(literal);
            });
    assertThat(thrown.getCause(), instanceOf(DateTimeParseException.class));
  }

  @Test
  public void parseLiteralFailsIfNotStringValue() {
    CoercingParseLiteralException thrown =
        assertThrows(
            CoercingParseLiteralException.class,
            () -> new DateTimeCoercing().parseLiteral(new Object()));

    assertThat(thrown.getMessage(), containsString("Object"));
    assertThat(thrown.getMessage(), containsString("StringValue"));
  }

  @Test
  public void parseLiteralNull() {
    assertThrows(
        CoercingParseLiteralException.class, () -> new DateTimeCoercing().parseLiteral(null));
  }

  @Test
  public void serialize() {
    String actual = new DateTimeCoercing().serialize(new DateTime(zonedExample));
    assertEquals(zonedString, actual);
  }

  @Test
  public void serializeInvalidType() {
    CoercingSerializeException thrown =
        assertThrows(
            CoercingSerializeException.class, () -> new DateTimeCoercing().serialize(new Object()));
    assertThat(thrown.getCause(), instanceOf(ClassCastException.class));
  }

  @Test
  public void serializeNull() {
    CoercingSerializeException thrown =
        assertThrows(
            CoercingSerializeException.class, () -> new DateTimeCoercing().serialize(null));
    assertThat(thrown.getCause(), instanceOf(NullPointerException.class));
  }
}
