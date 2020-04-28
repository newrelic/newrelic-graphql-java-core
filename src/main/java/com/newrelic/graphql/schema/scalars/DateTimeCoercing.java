/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import java.time.ZonedDateTime;

/**
 * Coercion support for {@code DateTime} custom scalar class.
 *
 * <p>Serializes/deserializes as string representation of a {@code ZonedDataTime} instance.
 */
public class DateTimeCoercing extends StringCoercing<DateTime> {

  @Override
  protected DateTime parseFromString(String input) {
    return new DateTime(ZonedDateTime.parse(input));
  }

  @Override
  protected String serializeToString(DateTime input) {
    return input.getDateTime().toString();
  }
}
