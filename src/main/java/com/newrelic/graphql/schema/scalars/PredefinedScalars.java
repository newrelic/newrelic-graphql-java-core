/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema.scalars;

import graphql.language.ScalarTypeDefinition;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.SchemaParser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registration for all of the predefined scalars that ship with this library.
 *
 * <p>By default these are included in GraphQL execution instances built via {@code
 * SimpleGraphQLBuilder}.
 *
 * <p>Use the {@code addScalar} static function during class definition to include additional types.
 */
public final class PredefinedScalars {
  private static final Map<String, GraphQLScalarType> predefinedScalars = new HashMap<>();
  private static final Map<String, ScalarTypeDefinition> builtInScalars =
      new SchemaParser().parse("type Query {}").scalars();

  public static final GraphQLScalarType EpochMilliseconds =
      addScalar(
          "EpochMilliseconds", com.newrelic.graphql.schema.scalars.EpochMilliseconds.getCoercing());
  public static final GraphQLScalarType EpochSeconds =
      addScalar("EpochSeconds", com.newrelic.graphql.schema.scalars.EpochSeconds.getCoercing());
  public static final GraphQLScalarType Milliseconds =
      addScalar("Milliseconds", com.newrelic.graphql.schema.scalars.Milliseconds.getCoercing());
  public static final GraphQLScalarType Seconds =
      addScalar("Seconds", com.newrelic.graphql.schema.scalars.Seconds.getCoercing());
  public static final GraphQLScalarType Minutes =
      addScalar("Minutes", com.newrelic.graphql.schema.scalars.EpochSeconds.getCoercing());
  public static final GraphQLScalarType DateTime = addScalar("DateTime", new DateTimeCoercing());

  /**
   * @return Retrieve list of registered predefined scalars. By default these will be wired up when
   *     creating a GraphQL execution instance via {@code SimpleGraphQLBuilder}.
   */
  public static List<GraphQLScalarType> getPredefinedScalars() {
    return predefinedScalars.values().stream().collect(Collectors.toList());
  }

  /**
   * @param name Name of predefined scalar type definition
   * @return Scalar type definition instance or null if not found.
   */
  public static GraphQLScalarType get(String name) {
    return predefinedScalars.getOrDefault(name, null);
  }

  /**
   * @param name Name of predefined scalar type definition
   * @return Boolean indicating whether the type is registered or not.
   */
  public static boolean isPredefined(String name) {
    return predefinedScalars.containsKey(name);
  }

  /**
   * @param name Name of a scalar type definition
   * @return Boolean indicating whether it is a built-in GraphQL scalar (i.e. String)
   */
  public static boolean isBuiltin(String name) {
    return builtInScalars.containsKey(name);
  }

  private PredefinedScalars() {}

  private static GraphQLScalarType addScalar(String name, Coercing coercing) {
    return addScalar(GraphQLScalarType.newScalar().name(name).coercing(coercing).build());
  }

  private static GraphQLScalarType addScalar(GraphQLScalarType type) {
    predefinedScalars.put(type.getName(), type);
    return type;
  }
}
