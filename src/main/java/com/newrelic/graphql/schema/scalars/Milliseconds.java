/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import graphql.schema.Coercing;

/** Custom scalar representing a specific length of time in milliseconds */
public class Milliseconds extends DurationWrapper<Milliseconds> {

  /** @param value Number of milliseconds */
  public Milliseconds(Number value) {
    super(value, (n) -> buildDuration(n, 6));
  }

  /** @return Coercion instance for converting numbers to Milliseconds */
  public static Coercing<Milliseconds, Number> getCoercing() {
    return new NumberCoercing<Milliseconds>() {
      @Override
      protected Milliseconds fromNumber(Number value) {
        return new Milliseconds(value);
      }
    };
  }
}
