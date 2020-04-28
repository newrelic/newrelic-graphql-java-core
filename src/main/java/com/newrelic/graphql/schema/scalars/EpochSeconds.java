/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import graphql.schema.Coercing;

/** Custom scalar representing a specific number of seconds since epoch */
public class EpochSeconds extends InstantWrapper<EpochSeconds> {

  /** @param value Incoming number of seconds since epoch */
  public EpochSeconds(Number value) {
    super(value, (n) -> buildInstant(n, 9));
  }

  /** @return Coercion instance for converting numbers to EpochSeconds */
  public static Coercing<EpochSeconds, Number> getCoercing() {
    return new NumberCoercing<EpochSeconds>() {
      @Override
      protected EpochSeconds fromNumber(Number value) {
        return new EpochSeconds(value);
      }
    };
  }
}
