/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;

/**
 * If your schema involves interfaces or unions, type resolvers are needed to determine the outgoing
 * object type that an interface is implemented by.
 *
 * <p>This default class assumes your Java class names align with your schema type names.
 *
 * <p>If your type naming doesn't line up, you'll need to wire up your own resolver.
 */
public class DefaultTypeResolver implements TypeResolver {
  @Override
  public GraphQLObjectType getType(TypeResolutionEnvironment env) {
    String className = env.getObject().getClass().getSimpleName();
    return env.getSchema().getObjectType(className);
  }
}
