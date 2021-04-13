/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema;

import com.newrelic.graphql.schema.scalars.PredefinedScalars;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.AsyncSerialExecutionStrategy;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.SimpleDataFetcherExceptionHandler;
import graphql.language.InterfaceTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.language.UnionTypeDefinition;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.TypeResolver;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This builder provides a simple fluent interface for wiring up your schema for runtime execution.
 * It also provides defaults for a few common scenarios to streamline getting started:
 *
 * <ul>
 *   <li>Default registration for scalar types as Strings until overridden otherwise
 *   <li>Default type resolving for interfaces/unions, so if your Java class names match your
 *       GraphQL type names no further customization is required
 *   <li>Simple builder methods for common operations (i.e. providing the {@code fetcher} for a
 *       field)
 * </ul>
 *
 * <p>If you need access to the lower level {@code RuntimeWiring.Builder}, use the {@code configure}
 * function to provide a callback. If you need access to the {@code GraphQL.Builder}, then call
 * {@code builder()} instead of {@code build()} to get the in-flight builder instance back.
 *
 * <pre>
 *   Reader schemaReader = //... Read your GraphQL SDL from somewhere
 *   GraphQL graphQL = new SimpleGraphQLBuilder(schemaReader)
 *               .fetcher("Query", "myField", new QueryMyFieldFetcher())
 *               .build()
 * </pre>
 */
public class SimpleGraphQLBuilder {
  static Logger logger = LoggerFactory.getLogger(SimpleGraphQLBuilder.class);

  private static final SchemaParser schemaParser = new SchemaParser();
  private static final DefaultTypeResolver defaultTypeResolver = new DefaultTypeResolver();

  private final Reader schemaReader;

  private DataFetcherExceptionHandler exceptionHandler;
  private HashMap<String, DataFetcher> fetchers;
  private HashMap<String, GraphQLScalarType> scalars;
  private HashMap<String, TypeResolver> typeResolvers;
  private IConfigureSimpleGraphQLBuilder configurator;
  private boolean usePredefinedScalars;

  /** @param schema Reader containing your GraphQL SDL definition */
  public SimpleGraphQLBuilder(Reader schema) {
    this.schemaReader = schema;

    this.exceptionHandler = new SimpleDataFetcherExceptionHandler();
    this.fetchers = new HashMap<>();
    this.scalars = new HashMap<>();
    this.typeResolvers = new HashMap<>();
    this.usePredefinedScalars = true;
  }

  /**
   * @return Constructs an instance of the GraphQL execution object with your builder configuration.
   */
  public GraphQL build() {
    return builder().build();
  }

  /** @return Returns intermediate builder object to allow additional configuration. */
  public GraphQL.Builder builder() {
    TypeDefinitionRegistry typeRegistry = schemaParser.parse(schemaReader);

    RuntimeWiring.Builder runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring();
    wireUpDataFetchers(runtimeWiringBuilder, fetchers);
    wireUpScalars(runtimeWiringBuilder, scalars, typeRegistry);
    wireUpTypeResolvers(runtimeWiringBuilder, typeResolvers, typeRegistry);

    if (configurator != null) {
      configurator.configure(typeRegistry, runtimeWiringBuilder);
    }

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    SchemaGenerator.Options options = SchemaGenerator.Options.defaultOptions();
    GraphQLSchema schema =
        schemaGenerator.makeExecutableSchema(options, typeRegistry, runtimeWiringBuilder.build());

    return GraphQL.newGraphQL(schema)
        .queryExecutionStrategy(new AsyncExecutionStrategy(exceptionHandler))
        .mutationExecutionStrategy(new AsyncSerialExecutionStrategy(exceptionHandler));
  }

  /**
   * @param typeName GraphQL type name that this data fetcher applies to
   * @param fieldName Name of field on the GraphQL type that the data fetcher applies to
   * @param fetcher Instance of DataFetcher to apply to the provided type and field name
   * @return Fluent builder instance
   */
  public SimpleGraphQLBuilder fetcher(String typeName, String fieldName, DataFetcher fetcher) {
    this.fetchers.put(String.format("%s.%s", typeName, fieldName), fetcher);
    return this;
  }

  /**
   * @param incoming Map from field identifying strings to DataFetcher instances. Strings must be in
   *     the format "Type.field"
   * @return Fluent builder instance
   */
  public SimpleGraphQLBuilder fetchers(Map<String, DataFetcher> incoming) {
    this.fetchers.putAll(incoming);
    return this;
  }

  /**
   * @param incoming Map from scalar names to scalar definition types
   * @return Fluent builder instance
   */
  public SimpleGraphQLBuilder scalars(Map<String, GraphQLScalarType> incoming) {
    this.scalars.putAll(incoming);
    return this;
  }

  /**
   * @param name Scalar name
   * @param type Scalar type definition
   * @return Fluent builder instance
   */
  public SimpleGraphQLBuilder scalar(String name, GraphQLScalarType type) {
    this.scalars.put(name, type);
    return this;
  }

  /**
   * @param incoming Map from type name to a custom type resolver
   * @return Fluent builder instance
   */
  public SimpleGraphQLBuilder typeResolvers(Map<String, TypeResolver> incoming) {
    this.typeResolvers.putAll(incoming);
    return this;
  }

  /**
   * @param name Type name
   * @param resolver Type resolver instance
   * @return Fluent builder instance
   */
  public SimpleGraphQLBuilder typeResolver(String name, TypeResolver resolver) {
    this.typeResolvers.put(name, resolver);
    return this;
  }

  /**
   * @param exceptionHandler Exception handler to pass to the GraphQL execution strategy
   * @return Fluent builder instance
   */
  public SimpleGraphQLBuilder exceptionHandler(DataFetcherExceptionHandler exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
    return this;
  }

  /**
   * @param usePredefinedScalars Controls whether the predefined scalars from this library should be
   *     automatically registered when building the GraphQL execution instance.
   * @return Fluent builder instance
   */
  public SimpleGraphQLBuilder usePredefinedScalars(boolean usePredefinedScalars) {
    this.usePredefinedScalars = usePredefinedScalars;
    return this;
  }

  /** Interface for providing a custom configuration callback to the SimpleGraphQLBuilder. */
  public interface IConfigureSimpleGraphQLBuilder {
    /**
     * @param registry Type registry loaded with your schema
     * @param runtimeWiringBuilder Runtime wiring object being used to create the GraphQL execution
     *     instance.
     */
    void configure(TypeDefinitionRegistry registry, RuntimeWiring.Builder runtimeWiringBuilder);
  }

  /**
   * @param configurator Custom callback to further configure your GraphQL execution instance.
   * @return Fluent builder instance
   */
  public SimpleGraphQLBuilder configure(IConfigureSimpleGraphQLBuilder configurator) {
    this.configurator = configurator;
    return this;
  }

  private static void wireUpDataFetchers(
      RuntimeWiring.Builder builder, Map<String, DataFetcher> fetchers) {
    fetchers.forEach(
        (key, fetcher) -> {
          String[] parts = key.split("[.]");
          if (parts.length == 2) {
            builder.type(parts[0], t -> t.dataFetcher(parts[1], fetcher));
          } else {
            logger.warn("Unexpected field specification '{}'", key);
          }
        });
  }

  private void wireUpScalars(
      RuntimeWiring.Builder builder,
      Map<String, GraphQLScalarType> scalars,
      TypeDefinitionRegistry typeRegistry) {
    typeRegistry
        .scalars()
        .forEach(
            (name, typeDefinition) -> {
              if (!PredefinedScalars.isBuiltin(name)) {
                // User defined scalar
                GraphQLScalarType scalarType = scalars.get(name);

                // Predefined scalar from our library
                if (scalarType == null && usePredefinedScalars) {
                  scalarType = PredefinedScalars.get(name);
                }

                // Fall back to default scalar definition
                if (scalarType == null) {
                  String description =
                      typeDefinition.getDescription() != null
                          ? typeDefinition.getDescription().content
                          : null;
                  scalarType = defaultScalarType(name, description);
                }

                builder.scalar(scalarType);
              }
            });
  }

  private static void wireUpTypeResolvers(
      RuntimeWiring.Builder builder,
      Map<String, TypeResolver> typeResolvers,
      TypeDefinitionRegistry typeRegistry) {
    typeRegistry
        .getTypes(InterfaceTypeDefinition.class)
        .forEach(type -> resolverForType(builder, typeResolvers, type));
    typeRegistry
        .getTypes(UnionTypeDefinition.class)
        .forEach(type -> resolverForType(builder, typeResolvers, type));
  }

  private static void resolverForType(
      RuntimeWiring.Builder builder, Map<String, TypeResolver> typeResolvers, TypeDefinition type) {
    String typeName = type.getName();
    TypeResolver resolver = typeResolvers.getOrDefault(typeName, defaultTypeResolver);
    builder.type(typeName, t -> t.typeResolver(resolver));
  }

  private static GraphQLScalarType defaultScalarType(String name, String description) {
    return GraphQLScalarType.newScalar()
        .name(name)
        .description(description)
        .coercing(Scalars.GraphQLString.getCoercing())
        .build();
  }
}
