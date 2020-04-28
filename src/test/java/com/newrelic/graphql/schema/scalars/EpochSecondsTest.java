/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import org.junit.Test;

public class EpochSecondsTest {

  private final EpochSeconds seconds = new EpochSeconds(1L);

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
}
