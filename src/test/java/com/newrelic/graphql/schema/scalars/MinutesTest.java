/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import static org.junit.Assert.*;

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

public class MinutesTest {

  private final Minutes minutes = new Minutes(1L);
  private final GraphQLInputMapper mapper =
      new GraphQLInputMapper(this.getClass().getPackage().getName());

  @Test
  public void testEquals() {
    assertTrue(minutes.equals(minutes));
    assertTrue(minutes.equals(new Minutes(1L)));
    assertFalse(minutes.equals(new Minutes(2L)));
    assertFalse(minutes.equals(new Object()));
  }

  @Test
  public void checkHashCode() {
    assertEquals(minutes.hashCode(), new Minutes(1L).hashCode());
  }

  @Test
  public void getDuration() {
    assertEquals(minutes.getDuration(), Duration.ofMinutes(1L));
  }

  @Test
  public void floatingPointDurationIgnored() {
    Minutes floating = new Minutes(1.234);
    assertEquals(Duration.ofMinutes(1), floating.getDuration());
  }

  @Test
  public void convertToScalarFromInstanceOfSelf() throws ClassNotFoundException {
    Object actual = mapper.convert(new Minutes(1000), PredefinedScalars.Minutes);

    assertEquals(new Minutes(1000), actual);
  }

  @Test
  public void convertToContainerObjectFromInstanceOfSelf() throws ClassNotFoundException {
    HashMap<String, Object> raw = new HashMap<>();
    raw.put("minutes", new Minutes(1000));
    Object actual = mapper.convert(raw, MinutesContainer.GraphQLType);

    assertEquals(new MinutesContainer(new Minutes(1000)), actual);
  }

  @Test
  public void convertToListOfContainerObjectsFromInstanceOfSelf() throws ClassNotFoundException {
    Map<String, Object> item = new HashMap<>();
    item.put("minutes", new Minutes(1000));

    List<Map<String, Object>> raw = new ArrayList<>();
    raw.add(item);

    Object actual = mapper.convert(raw, new GraphQLList(MinutesContainer.GraphQLType));

    List<MinutesContainer> expected = new ArrayList<>();
    expected.add(new MinutesContainer(new Minutes(1000)));
    assertEquals(expected, actual);
  }
}

class MinutesContainer {
  public final Minutes minutes;

  public static final GraphQLInputObjectType GraphQLType =
      GraphQLInputObjectType.newInputObject().name("MinutesContainer").build();

  public MinutesContainer(@JsonProperty("minutes") Minutes minutes) {
    this.minutes = minutes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MinutesContainer that = (MinutesContainer) o;

    return Objects.equals(minutes, that.minutes);
  }

  @Override
  public int hashCode() {
    return minutes != null ? minutes.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "MinutesContainer{" + "minutes=" + minutes + '}';
  }
}
