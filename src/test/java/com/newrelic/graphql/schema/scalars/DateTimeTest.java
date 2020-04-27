package com.newrelic.graphql.schema.scalars;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.time.ZonedDateTime;
import org.junit.Test;

public class DateTimeTest {

  private static final String zonedString = "1978-09-01T10:15:30-09:00";
  private static final ZonedDateTime zonedExample = ZonedDateTime.parse(zonedString);
  private static final DateTime dateTime = new DateTime(zonedExample);

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
}
