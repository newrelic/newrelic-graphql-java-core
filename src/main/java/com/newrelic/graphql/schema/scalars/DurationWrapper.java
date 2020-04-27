package com.newrelic.graphql.schema.scalars;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;

/**
 * Wrapper for a Duration. Intended for use through more specific specialized classes like {@code
 * Seconds}.
 *
 * @param <T> More specific wrapper class type.
 */
public class DurationWrapper<T extends DurationWrapper> extends NumberCoercing.NumberWrapper {
  private Duration duration;

  /**
   * @param value Incoming number value. Supports int, long and float
   * @param factory Converter for turning incoming value to a {@code Duration}
   */
  public DurationWrapper(Number value, DurationFactory factory) {
    super(value);
    duration = factory.of(value);
  }

  /** @return Duration instance */
  public Duration getDuration() {
    return duration;
  }

  protected static Duration buildDuration(Number n, int factor) {
    BigDecimal dec = toBigDecimal(n);
    return Duration.ofSeconds(0, dec.scaleByPowerOfTen(factor).longValue());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    DurationWrapper<?> that = (DurationWrapper<?>) o;

    return Objects.equals(duration, that.duration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), duration);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "{" + "duration=" + duration + '}';
  }

  /**
   * Interface for defining conversion from general numeric type to a Duration. Implementations
   * defined by more specific child wrapper classes.
   */
  public interface DurationFactory {
    Duration of(Number value);
  }
}
