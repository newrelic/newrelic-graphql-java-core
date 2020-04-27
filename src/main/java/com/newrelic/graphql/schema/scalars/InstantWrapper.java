package com.newrelic.graphql.schema.scalars;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Wrapper for a Instant. Intended for use through more specific specialized classes like {@code
 * EpochSeconds}.
 *
 * @param <T> More specific wrapper class type.
 */
public class InstantWrapper<T extends InstantWrapper> extends NumberCoercing.NumberWrapper {
  private Instant instant;

  /**
   * @param value Incoming number value. Supports int, long and float
   * @param factory Converter for turning incoming value to a {@code Instant}
   */
  public InstantWrapper(Number value, InstantFactory factory) {
    super(value);
    instant = factory.of(value);
  }

  protected static Instant buildInstant(Number n, int factor) {
    BigDecimal dec = toBigDecimal(n);
    return Instant.ofEpochSecond(0, dec.scaleByPowerOfTen(factor).longValue());
  }

  /** @return Instant instance */
  public Instant getInstant() {
    return instant;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    InstantWrapper<?> that = (InstantWrapper<?>) o;

    return Objects.equals(instant, that.instant);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), instant);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "{" + "instant=" + instant + '}';
  }

  /**
   * Interface for defining conversion from general numeric type to a Instant. Implementations
   * defined by more specific child wrapper classes.
   */
  public interface InstantFactory {
    Instant of(Number value);
  }
}
