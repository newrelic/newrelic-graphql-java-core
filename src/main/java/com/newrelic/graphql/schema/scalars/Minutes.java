/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import com.fasterxml.jackson.annotation.JsonCreator;
import graphql.schema.Coercing;
import java.time.Duration;
import java.util.Map;

/** Custom scalar representing a specific length of time in minutes */
public class Minutes extends DurationWrapper<Minutes> {

  /** @param value Number of minutes */
  public Minutes(Number value) {
    super(value, (n) -> Duration.ofMinutes(n.longValue()));
  }

  // Support for serialization from instances of our own type
  @JsonCreator
  private Minutes(Map<String, Object> props) {
    this(rawValueFromProps(props));
  }

  /** @return Coercion instance for converting numbers to Minutes */
  public static Coercing<Minutes, Number> getCoercing() {
    return new NumberCoercing<Minutes>() {
      @Override
      protected Minutes fromNumber(Number value) {
        return new Minutes(value);
      }
    };
  }
}
