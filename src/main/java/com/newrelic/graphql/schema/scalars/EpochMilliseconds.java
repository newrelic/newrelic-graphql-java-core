/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import graphql.schema.Coercing;

/** Custom scalar representing a specific number of milliseconds since epoch */
public class EpochMilliseconds extends InstantWrapper<EpochMilliseconds> {

  /** @param value Incoming number of milliseconds since epoch */
  public EpochMilliseconds(Number value) {
    super(value, (n) -> buildInstant(n, 6));
  }

  /** @return Coercion instance for converting numbers to EpochMilliseconds */
  public static Coercing<EpochMilliseconds, Number> getCoercing() {
    return new NumberCoercing<EpochMilliseconds>() {
      @Override
      protected EpochMilliseconds fromNumber(Number value) {
        return new EpochMilliseconds(value);
      }
    };
  }
}
