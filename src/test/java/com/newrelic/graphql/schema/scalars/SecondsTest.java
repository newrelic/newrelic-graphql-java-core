package com.newrelic.graphql.schema.scalars;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import org.junit.Test;

public class SecondsTest {

  private final Seconds seconds = new Seconds(1L);

  @Test
  public void testEquals() {
    assertTrue(seconds.equals(seconds));
    assertTrue(seconds.equals(new Seconds(1L)));
    assertFalse(seconds.equals(new Seconds(2L)));
    assertFalse(seconds.equals(new Object()));
  }

  @Test
  public void checkHashCode() {
    assertEquals(seconds.hashCode(), new Seconds(1L).hashCode());
  }

  @Test
  public void getDuration() {
    assertEquals(seconds.getDuration(), Duration.ofSeconds(1L));
  }

  @Test
  public void floatingPointDuration() {
    Seconds floating = new Seconds(1.234);
    assertEquals(Duration.ofSeconds(1, 233999999L), floating.getDuration());
  }
}
