package com.newrelic.graphql.schema.scalars;

import static org.junit.Assert.*;

import java.time.Duration;
import org.junit.Test;

public class MinutesTest {

  private final Minutes minutes = new Minutes(1L);

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
}
