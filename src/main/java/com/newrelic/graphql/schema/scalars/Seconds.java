/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import com.fasterxml.jackson.annotation.JsonCreator;
import graphql.schema.Coercing;
import java.util.Map;

/** Custom scalar representing a specific length of time in seconds */
public class Seconds extends DurationWrapper<Seconds> {

  /** @param value Number of seconds */
  public Seconds(Number value) {
    super(value, (n) -> buildDuration(n, 9));
  }

  // Support for serialization from instances of our own type
  @JsonCreator
  private Seconds(Map<String, Object> props) {
    this(rawValueFromProps(props));
  }

  /** @return Coercion instance for converting numbers to Seconds */
  public static Coercing<Seconds, Number> getCoercing() {
    return new NumberCoercing<Seconds>() {
      @Override
      protected Seconds fromNumber(Number value) {
        return new Seconds(value);
      }
    };
  }
}
