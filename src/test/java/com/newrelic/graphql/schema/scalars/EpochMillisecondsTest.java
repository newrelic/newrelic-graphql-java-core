package com.newrelic.graphql.schema.scalars;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import org.junit.Test;

public class EpochMillisecondsTest {

  private final EpochMilliseconds millis = new EpochMilliseconds(1L);

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
}
