package com.newrelic.graphql.schema.scalars;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import org.junit.Test;

public class MillisecondsTest {

  private final Milliseconds millis = new Milliseconds(1L);

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
}
