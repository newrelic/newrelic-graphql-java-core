/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

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

  // Support for serialization from instances of our own type
  @JsonCreator
  private DateTime(Map<String, Object> props) {
    this(rawValueFromProps(props));
  }

  protected static ZonedDateTime rawValueFromProps(Map<String, Object> props) {
    Map<String, Object> value = cast(props.get("dateTime"));
    if (value == null) {
      throw new IllegalArgumentException("Can't deserialize from properties missing 'dateTime'");
    }

    Map<String, Object> zone = cast(value.get("zone"));
    if (zone == null) {
      throw new IllegalArgumentException(
          "Can't deserialize from properties missing 'dateTime.zone'");
    }

    return ZonedDateTime.of(
        (Integer) value.get("year"),
        (Integer) value.get("monthValue"),
        (Integer) value.get("dayOfMonth"),
        (Integer) value.get("hour"),
        (Integer) value.get("minute"),
        (Integer) value.get("second"),
        (Integer) value.get("nano"),
        ZoneId.of((String) zone.get("id")));
  }

  @SuppressWarnings("unchecked")
  private static <T> T cast(Object rawValue) {
    return (T) rawValue;
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
