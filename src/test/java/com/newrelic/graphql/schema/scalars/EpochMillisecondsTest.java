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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.Test;

public class EpochMillisecondsTest {

  private final EpochMilliseconds millis = new EpochMilliseconds(1L);
  private final GraphQLInputMapper mapper =
      new GraphQLInputMapper(this.getClass().getPackage().getName());

  @Test
  public void testEquals() {
    assertTrue(millis.equals(millis));
    assertTrue(millis.equals(new EpochMilliseconds(1L)));
    assertFalse(millis.equals(new EpochMilliseconds(2L)));
    assertFalse(millis.equals(new Object()));
    assertFalse(millis.equals(null));
  }

  @Test
  public void checkHashCode() {
    assertEquals(millis.hashCode(), new EpochMilliseconds(1L).hashCode());
  }

  @Test
  public void getDuration() {
    assertEquals(millis.getInstant(), Instant.ofEpochMilli(1L));
  }

  @Test
  public void floatingPointDuration() {
    EpochMilliseconds floating = new EpochMilliseconds(1.234);
    assertEquals(Instant.ofEpochSecond(0, 1233999L), floating.getInstant());
  }

  @Test
  public void convertToScalarFromInstanceOfSelf() throws ClassNotFoundException {
    Object actual =
        mapper.convert(new EpochMilliseconds(1000), PredefinedScalars.EpochMilliseconds);

    assertEquals(new EpochMilliseconds(1000), actual);
  }

  @Test
  public void convertToContainerObjectFromInstanceOfSelf() throws ClassNotFoundException {
    HashMap<String, Object> raw = new HashMap<>();
    raw.put("millis", new EpochMilliseconds(1000));
    Object actual = mapper.convert(raw, EpochMillisecondsContainer.GraphQLType);

    assertEquals(new EpochMillisecondsContainer(new EpochMilliseconds(1000)), actual);
  }

  @Test
  public void convertToListOfContainerObjectsFromInstanceOfSelf() throws ClassNotFoundException {
    Map<String, Object> item = new HashMap<>();
    item.put("millis", new EpochMilliseconds(1000));

    List<Map<String, Object>> raw = new ArrayList<>();
    raw.add(item);

    Object actual = mapper.convert(raw, new GraphQLList(EpochMillisecondsContainer.GraphQLType));

    List<EpochMillisecondsContainer> expected = new ArrayList<>();
    expected.add(new EpochMillisecondsContainer(new EpochMilliseconds(1000)));
    assertEquals(expected, actual);
  }
}

class EpochMillisecondsContainer {
  public final EpochMilliseconds millis;

  public static final GraphQLInputObjectType GraphQLType =
      GraphQLInputObjectType.newInputObject().name("EpochMillisecondsContainer").build();

  public EpochMillisecondsContainer(@JsonProperty("millis") EpochMilliseconds millis) {
    this.millis = millis;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EpochMillisecondsContainer that = (EpochMillisecondsContainer) o;

    return Objects.equals(millis, that.millis);
  }

  @Override
  public int hashCode() {
    return millis != null ? millis.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "EpochMillisecondsContainer{" + "millis=" + millis + '}';
  }
}
