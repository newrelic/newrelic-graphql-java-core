/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.newrelic.graphql.mapper.GraphQLInputMapper;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.Test;

public class DateTimeTest {

  private static final String zonedString = "1978-09-01T10:15:30-09:00";
  private static final ZonedDateTime zonedExample = ZonedDateTime.parse(zonedString);
  private static final DateTime dateTime = new DateTime(zonedExample);
  private final GraphQLInputMapper mapper =
      new GraphQLInputMapper(this.getClass().getPackage().getName());

  @Test
  public void checkToString() {
    String actual = dateTime.toString();
    assertEquals(zonedString, actual);
  }

  @Test
  public void checkEquals() {
    assertTrue(dateTime.equals(new DateTime(zonedExample)));
  }

  @Test
  public void checkNotEquals() {
    assertFalse(dateTime.equals(new Object()));
  }

  @Test
  public void checkHashCode() {
    assertEquals(dateTime.hashCode(), new DateTime(zonedExample).hashCode());
  }

  @Test
  public void convertToScalarFromInstanceOfSelf() throws ClassNotFoundException {
    // This tests conversion in the format that GraphQLInput mapper may see for a plain DateTime
    // field.
    // The GraphQL engine will have turned our scalar into a DateTime already, but converting must
    // be safe
    // for some generated code cases, so make sure it round-trips fine.
    Object actual = mapper.convert(new DateTime(zonedExample), PredefinedScalars.DateTime);

    assertEquals(new DateTime(zonedExample), actual);
  }

  @Test
  public void convertToContainerObjectFromInstanceOfSelf() throws ClassNotFoundException {
    // This tests conversion in the format that GraphQLInput mapper may see for DateTimes on an
    // input object.
    // The GraphQL engine will have turned our object into a Map containing our DateTime scalar,
    // so this Map looks like what the DataFetchingEnvironment arguments would hold.
    HashMap<String, Object> raw = new HashMap<>();
    raw.put("dateTime", new DateTime(zonedExample));
    Object actual = mapper.convert(raw, DateTimeContainer.GraphQLType);

    assertEquals(new DateTimeContainer(new DateTime(zonedExample)), actual);
  }

  @Test
  public void convertToListOfContainerObjectsFromInstanceOfSelf() throws ClassNotFoundException {
    Map<String, Object> item = new HashMap<>();
    item.put("dateTime", new DateTime(zonedExample));

    List<Map<String, Object>> raw = new ArrayList<>();
    raw.add(item);

    Object actual = mapper.convert(raw, new GraphQLList(DateTimeContainer.GraphQLType));

    List<DateTimeContainer> expected = new ArrayList<>();
    expected.add(new DateTimeContainer(new DateTime(zonedExample)));
    assertEquals(expected, actual);
  }
}

class DateTimeContainer {
  public final DateTime dateTime;

  public static final GraphQLInputObjectType GraphQLType =
      GraphQLInputObjectType.newInputObject().name("DateTimeContainer").build();

  public DateTimeContainer(@JsonProperty("dateTime") DateTime dateTime) {
    this.dateTime = dateTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DateTimeContainer that = (DateTimeContainer) o;

    return Objects.equals(dateTime, that.dateTime);
  }

  @Override
  public int hashCode() {
    return dateTime != null ? dateTime.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "DateTimeContainer{" + "dateTime=" + dateTime + '}';
  }
}
