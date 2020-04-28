/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;

import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DateTimeCoercingTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

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
    thrown.expect(CoercingParseValueException.class);
    thrown.expectCause(isA(DateTimeParseException.class));

    new DateTimeCoercing().parseValue("not a date");
  }

  @Test
  public void parseValueNull() {
    thrown.expect(CoercingParseValueException.class);
    thrown.expectCause(isA(NullPointerException.class));

    new DateTimeCoercing().parseValue(null);
  }

  @Test
  public void parseLiteral() {
    StringValue literal = new StringValue(zonedString);
    DateTime actual = new DateTimeCoercing().parseLiteral(literal);
    assertEquals(new DateTime(zonedExample), actual);
  }

  @Test
  public void parseLiteralInvalidGuid() {
    thrown.expect(CoercingParseLiteralException.class);
    thrown.expectCause(isA(DateTimeParseException.class));

    StringValue literal = new StringValue("not a date");
    new DateTimeCoercing().parseLiteral(literal);
  }

  @Test
  public void parseLiteralFailsIfNotStringValue() {
    thrown.expect(CoercingParseLiteralException.class);
    thrown.expectMessage(containsString("Object"));
    thrown.expectMessage(containsString("StringValue"));

    new DateTimeCoercing().parseLiteral(new Object());
  }

  @Test
  public void parseLiteralNull() {
    thrown.expect(CoercingParseLiteralException.class);

    new DateTimeCoercing().parseLiteral(null);
  }

  @Test
  public void serialize() {
    String actual = new DateTimeCoercing().serialize(new DateTime(zonedExample));
    assertEquals(zonedString, actual);
  }

  @Test
  public void serializeInvalidType() {
    thrown.expect(CoercingSerializeException.class);
    thrown.expectCause(isA(ClassCastException.class));

    new DateTimeCoercing().serialize(new Object());
  }

  @Test
  public void serializeNull() {
    thrown.expect(CoercingSerializeException.class);
    thrown.expectCause(isA(NullPointerException.class));

    new DateTimeCoercing().serialize(null);
  }
}
