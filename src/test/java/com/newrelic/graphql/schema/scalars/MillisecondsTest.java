/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.newrelic.graphql.mapper.GraphQLInputMapper;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.Test;

public class MillisecondsTest {

  private final Milliseconds millis = new Milliseconds(1L);
  private final GraphQLInputMapper mapper =
      new GraphQLInputMapper(this.getClass().getPackage().getName());

  @Test
  public void testEquals() {
    assertTrue(millis.equals(millis));
    assertTrue(millis.equals(new Milliseconds(1L)));
    assertFalse(millis.equals(new Milliseconds(2L)));
    assertFalse(millis.equals(new Object()));
  }

  @Test
  public void checkHashCode() {
    assertEquals(millis.hashCode(), new Milliseconds(1L).hashCode());
  }

  @Test
  public void getDuration() {
    assertEquals(millis.getDuration(), Duration.ofMillis(1L));
  }

  @Test
  public void floatingPointDuration() {
    Milliseconds floating = new Milliseconds(1.234);
    assertEquals(Duration.ofSeconds(0, 1233999L), floating.getDuration());
  }

  @Test
  public void convertToScalarFromInstanceOfSelf() throws ClassNotFoundException {
    Object actual = mapper.convert(new Milliseconds(1000), PredefinedScalars.Milliseconds);

    assertEquals(new Milliseconds(1000), actual);
  }

  @Test
  public void convertToContainerObjectFromInstanceOfSelf() throws ClassNotFoundException {
    HashMap<String, Object> raw = new HashMap<>();
    raw.put("millis", new Milliseconds(1000));
    Object actual = mapper.convert(raw, MillisecondsContainer.GraphQLType);

    assertEquals(new MillisecondsContainer(new Milliseconds(1000)), actual);
  }

  @Test
  public void convertToListOfContainerObjectsFromInstanceOfSelf() throws ClassNotFoundException {
    Map<String, Object> item = new HashMap<>();
    item.put("millis", new Milliseconds(1000));

    List<Map<String, Object>> raw = new ArrayList<>();
    raw.add(item);

    Object actual = mapper.convert(raw, new GraphQLList(MillisecondsContainer.GraphQLType));

    List<MillisecondsContainer> expected = new ArrayList<>();
    expected.add(new MillisecondsContainer(new Milliseconds(1000)));
    assertEquals(expected, actual);
  }
}

class MillisecondsContainer {
  public final Milliseconds millis;

  public static final GraphQLInputObjectType GraphQLType =
      GraphQLInputObjectType.newInputObject().name("MillisecondsContainer").build();

  public MillisecondsContainer(@JsonProperty("millis") Milliseconds millis) {
    this.millis = millis;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MillisecondsContainer that = (MillisecondsContainer) o;

    return Objects.equals(millis, that.millis);
  }

  @Override
  public int hashCode() {
    return millis != null ? millis.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "MillisecondsContainer{" + "millis=" + millis + '}';
  }
}
