package com.newrelic.graphql.schema.scalars;

import graphql.schema.Coercing;

/** Custom scalar representing a specific length of time in seconds */
public class Seconds extends DurationWrapper<Seconds> {

  /** @param value Number of seconds */
  public Seconds(Number value) {
    super(value, (n) -> buildDuration(n, 9));
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
