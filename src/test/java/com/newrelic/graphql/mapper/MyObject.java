/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.mapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MyObject {
  private final String v1;
  private final String v2;

  @JsonCreator
  public MyObject(@JsonProperty("v1") String v1, @JsonProperty("v2") String v2) {
    this.v1 = v1;
    this.v2 = v2;
  }

  public String getV1() {
    return v1;
  }

  public String getV2() {
    return v2;
  }

  @Override
  public boolean equals(Object item) {
    if (item == null || !(item instanceof MyObject)) {
      return false;
    }

    MyObject other = (MyObject) item;
    return this.getV1().equals(other.getV1()) && this.getV2().equals(other.getV2());
  }

  public int hashCode() {
    throw new RuntimeException("hashCode not implemented");
  }
}
