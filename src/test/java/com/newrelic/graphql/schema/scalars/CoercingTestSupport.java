/*
 * Copyright 2026 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import java.util.Locale;

/** Default execution arguments for calling {@code Coercing} methods directly in tests. */
final class CoercingTestSupport {
  static final GraphQLContext CONTEXT = GraphQLContext.getDefault();
  static final Locale LOCALE = Locale.getDefault();
  static final CoercedVariables VARIABLES = CoercedVariables.emptyVariables();

  private CoercingTestSupport() {}
}
