package com.newrelic.graphql.schema.scalars;

import java.time.ZonedDateTime;

/**
 * ISO-8601 compliant custom scalar. Expects String inputs, converts to wrap a {@code ZonedDateTime}
 * object.
 */
public class DateTime {
  private final ZonedDateTime dateTime;

  /** @param dateTime Construct with a specific datetime */
  public DateTime(ZonedDateTime dateTime) {
    this.dateTime = dateTime;
  }

  /** @return Wrapped datetime object */
  public ZonedDateTime getDateTime() {
    return dateTime;
  }

  @Override
  public String toString() {
    return dateTime.toString();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof DateTime)) {
      return false;
    }

    return dateTime.equals(((DateTime) other).dateTime);
  }

  @Override
  public int hashCode() {
    return dateTime.hashCode();
  }
}
