package com.newrelic.graphql.schema.scalars;

import graphql.schema.Coercing;
import java.time.Duration;

/** Custom scalar representing a specific length of time in minutes */
public class Minutes extends DurationWrapper<Minutes> {

  /** @param value Number of minutes */
  public Minutes(Number value) {
    super(value, (n) -> Duration.ofMinutes(n.longValue()));
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
