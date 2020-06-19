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

public class EpochSecondsTest {

  private final EpochSeconds seconds = new EpochSeconds(1L);
  private final GraphQLInputMapper mapper =
      new GraphQLInputMapper(this.getClass().getPackage().getName());

  @Test
  public void testEquals() {
    assertTrue(seconds.equals(seconds));
    assertTrue(seconds.equals(new EpochSeconds(1L)));
    assertFalse(seconds.equals(new EpochSeconds(2L)));
    assertFalse(seconds.equals(new Object()));
  }

  @Test
  public void checkHashCode() {
    assertEquals(seconds.hashCode(), new EpochSeconds(1L).hashCode());
  }

  @Test
  public void getDuration() {
    assertEquals(seconds.getInstant(), Instant.ofEpochSecond(1L));
  }

  @Test
  public void floatingPointDuration() {
    EpochSeconds floating = new EpochSeconds(1.234);
    assertEquals(Instant.ofEpochSecond(1, 233999999L), floating.getInstant());
  }

  @Test
  public void convertToScalarFromInstanceOfSelf() throws ClassNotFoundException {
    Object actual = mapper.convert(new EpochSeconds(1000), PredefinedScalars.EpochSeconds);

    assertEquals(new EpochSeconds(1000), actual);
  }

  @Test
  public void convertToContainerObjectFromInstanceOfSelf() throws ClassNotFoundException {
    HashMap<String, Object> raw = new HashMap<>();
    raw.put("seconds", new EpochSeconds(1000));
    Object actual = mapper.convert(raw, EpochSecondsContainer.GraphQLType);

    assertEquals(new EpochSecondsContainer(new EpochSeconds(1000)), actual);
  }

  @Test
  public void convertToListOfContainerObjectsFromInstanceOfSelf() throws ClassNotFoundException {
    Map<String, Object> item = new HashMap<>();
    item.put("seconds", new EpochSeconds(1000));

    List<Map<String, Object>> raw = new ArrayList<>();
    raw.add(item);

    Object actual = mapper.convert(raw, new GraphQLList(EpochSecondsContainer.GraphQLType));

    List<EpochSecondsContainer> expected = new ArrayList<>();
    expected.add(new EpochSecondsContainer(new EpochSeconds(1000)));
    assertEquals(expected, actual);
  }
}

class EpochSecondsContainer {
  public final EpochSeconds seconds;

  public static final GraphQLInputObjectType GraphQLType =
      GraphQLInputObjectType.newInputObject().name("EpochSecondsContainer").build();

  public EpochSecondsContainer(@JsonProperty("seconds") EpochSeconds seconds) {
    this.seconds = seconds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EpochSecondsContainer that = (EpochSecondsContainer) o;

    return Objects.equals(seconds, that.seconds);
  }

  @Override
  public int hashCode() {
    return seconds != null ? seconds.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "EpochSecondsContainer{" + "seconds=" + seconds + '}';
  }
}
