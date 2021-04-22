/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.graphql.schema;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

import com.newrelic.graphql.schema.scalars.EpochMilliseconds;
import com.newrelic.graphql.schema.scalars.EpochSeconds;
import com.newrelic.graphql.schema.scalars.Milliseconds;
import com.newrelic.graphql.schema.scalars.Minutes;
import com.newrelic.graphql.schema.scalars.Seconds;
import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.SimpleDataFetcherExceptionHandler;
import graphql.scalar.GraphqlStringCoercing;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLScalarType;
import graphql.schema.TypeResolver;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.validation.ValidationError;
import graphql.validation.ValidationErrorType;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

public class SimpleGraphQLBuilderTest {

  final Reader schema =
      new StringReader(
          "\"Stringy description\"\n"
              + "scalar Stringy "
              + "scalar Numbery "
              + "type Query { read: Stringy } "
              + "\n\"Muted\"\n"
              + "type Mutation { write(str: Stringy, num: Numbery): Numbery } ");

  final StringReader schemaWithInterfaces =
      new StringReader(
          "interface MyInterface { id: ID } "
              + "type MyObject implements MyInterface { id: ID, value: String } "
              + "type Query { read: MyInterface   list: [MyInterface] }");

  final StringReader schemaWithUnions =
      new StringReader(
          "type MyObject { id: ID, value: String } "
              + "type AnotherObject { id: ID } "
              + "union MyUnion = MyObject | AnotherObject "
              + "type Query { read: MyUnion  list: [MyUnion] }");

  @Test
  public void buildWithFetcherMap() {
    DataFetcher fetcher = env -> "yup";

    Map<String, DataFetcher> fetchers = new HashMap<>();
    fetchers.put("Query.read", fetcher);

    GraphQL graphQL = new SimpleGraphQLBuilder(schema).fetchers(fetchers).build();

    ExecutionResult response = graphQL.execute("query { read }");
    assertThat(response.getData(), is(expectedResponse("read", "yup")));
  }

  @Test
  public void buildWithFetcher() {
    DataFetcher fetcher = env -> "yup";

    GraphQL graphQL = new SimpleGraphQLBuilder(schema).fetcher("Query", "read", fetcher).build();

    ExecutionResult response = graphQL.execute("query { read }");
    assertThat(response.getData(), is(expectedResponse("read", "yup")));
  }

  @Test
  public void buildDefaultsScalarsToString() {
    DataFetcher fetcher = env -> env.getArgument("num");

    GraphQL graphQL =
        new SimpleGraphQLBuilder(schema).fetcher("Mutation", "write", fetcher).build();

    ExecutionResult response = graphQL.execute("mutation { write(str: \"lol\", num: 42) }");
    assertThat(response.getErrors().size(), is(1));

    ValidationError error = (ValidationError) response.getErrors().get(0);
    assertThat(error.getValidationErrorType(), is(ValidationErrorType.WrongType));
  }

  @Test
  public void buildWithScalarMap() {
    DataFetcher fetcher = env -> env.getArgument("num");

    GraphQLScalarType numbery =
        GraphQLScalarType.newScalar()
            .name("Numbery")
            .coercing(Scalars.GraphQLInt.getCoercing())
            .build();

    Map<String, GraphQLScalarType> scalars = new HashMap<>();
    scalars.put(numbery.getName(), numbery);

    GraphQL graphQL =
        new SimpleGraphQLBuilder(schema)
            .fetcher("Mutation", "write", fetcher)
            .scalars(scalars)
            .build();

    ExecutionResult response = graphQL.execute("mutation { write(str: \"lol\", num: 42) }");

    assertThat(response.getData(), is(expectedResponse("write", 42)));
  }

  @Test
  public void buildWithScalar() {
    DataFetcher fetcher = env -> env.getArgument("num");

    GraphQLScalarType numbery =
        GraphQLScalarType.newScalar()
            .name("Numbery")
            .coercing(Scalars.GraphQLInt.getCoercing())
            .build();

    GraphQL graphQL =
        new SimpleGraphQLBuilder(schema)
            .fetcher("Mutation", "write", fetcher)
            .scalar(numbery.getName(), numbery)
            .build();

    ExecutionResult response = graphQL.execute("mutation { write(str: \"lol\", num: 42) }");

    assertThat(response.getData(), is(expectedResponse("write", 42)));
  }

  @Test
  public void buildWithDefaultTypeResolverForInterfaces() {
    DataFetcher fetcher = env -> new MyObject("1", "v");

    GraphQL graphQL =
        new SimpleGraphQLBuilder(schemaWithInterfaces).fetcher("Query", "read", fetcher).build();

    ExecutionResult response = graphQL.execute("query { read { id ... on MyObject { value }} }");

    Map<String, String> map = new HashMap<>();
    map.put("id", "1");
    map.put("value", "v");

    assertThat(response.getData(), is(expectedResponse("read", map)));
  }

  @Test
  public void buildWithExplicitSingleTypeResolver() {
    DataFetcher fetcher = env -> new MyObject("1", "v");
    AtomicBoolean called = new AtomicBoolean(false);

    TypeResolver resolver =
        env -> {
          // Make sure we're not hitting default resolver
          called.set(true);
          return env.getSchema().getObjectType("MyObject");
        };

    GraphQL graphQL =
        new SimpleGraphQLBuilder(schemaWithInterfaces)
            .fetcher("Query", "read", fetcher)
            .typeResolver("MyInterface", resolver)
            .build();

    ExecutionResult response = graphQL.execute("query { read { id ... on MyObject { value }} }");

    Map<String, String> map = new HashMap<>();
    map.put("id", "1");
    map.put("value", "v");

    assertThat(response.getData(), is(expectedResponse("read", map)));
    assertThat(called.get(), is(true));
  }

  @Test
  public void buildWithExplicitTypeResolverMapForInterfaces() {
    DataFetcher fetcher = env -> new MyObject("1", "v");
    AtomicBoolean called = new AtomicBoolean(false);

    TypeResolver resolver =
        env -> {
          // Make sure we're not hitting default resolver
          called.set(true);
          return env.getSchema().getObjectType("MyObject");
        };

    Map<String, TypeResolver> resolvers = new HashMap<>();
    resolvers.put("MyInterface", resolver);

    GraphQL graphQL =
        new SimpleGraphQLBuilder(schemaWithInterfaces)
            .fetcher("Query", "read", fetcher)
            .typeResolvers(resolvers)
            .build();

    ExecutionResult response = graphQL.execute("query { read { id ... on MyObject { value }} }");

    Map<String, String> map = new HashMap<>();
    map.put("id", "1");
    map.put("value", "v");

    assertThat(response.getData(), is(expectedResponse("read", map)));
    assertThat(called.get(), is(true));
  }

  @Test
  public void buildWithDefaultTypeResolverForListOfInterfaces() {
    ArrayList<MyObject> list = new ArrayList<>();
    list.add(new MyObject("1", "v"));
    list.add(new MyObject("2", "t"));

    DataFetcher fetcher = env -> list;

    GraphQL graphQL =
        new SimpleGraphQLBuilder(schemaWithInterfaces).fetcher("Query", "list", fetcher).build();

    ExecutionResult response = graphQL.execute("query { list { id ... on MyObject { value }} }");

    Map<String, String> map1 = new HashMap<>();
    map1.put("id", "1");
    map1.put("value", "v");

    Map<String, String> map2 = new HashMap<>();
    map2.put("id", "2");
    map2.put("value", "t");

    ArrayList<Map<String, String>> expectedList = new ArrayList<>();
    expectedList.add(map1);
    expectedList.add(map2);

    assertThat(response.getData(), is(expectedResponse("list", expectedList)));
  }

  @Test
  public void buildWithDefaultTypeResolverForUnions() {
    DataFetcher fetcher = env -> new MyObject("1", "v");

    GraphQL graphQL =
        new SimpleGraphQLBuilder(schemaWithUnions).fetcher("Query", "read", fetcher).build();

    ExecutionResult response = graphQL.execute("query { read { ... on MyObject { id value }} }");

    Map<String, String> map1 = new HashMap<>();
    map1.put("id", "1");
    map1.put("value", "v");

    assertThat(response.getData(), is(expectedResponse("read", map1)));
  }

  @Test
  public void buildWithExplicitTypeResolverForUnions() {
    DataFetcher fetcher = env -> new AnotherObject("1");
    AtomicBoolean called = new AtomicBoolean(false);

    TypeResolver resolver =
        env -> {
          // Make sure we're not hitting default resolver
          called.set(true);
          return env.getSchema().getObjectType("AnotherObject");
        };

    GraphQL graphQL =
        new SimpleGraphQLBuilder(schemaWithUnions)
            .fetcher("Query", "read", fetcher)
            .typeResolver("MyUnion", resolver)
            .build();

    ExecutionResult response = graphQL.execute("query { read { ... on AnotherObject { id }} }");

    Map<String, String> map1 = new HashMap<>();
    map1.put("id", "1");

    assertThat(response.getData(), is(expectedResponse("read", map1)));
    assertThat(called.get(), is(true));
  }

  @Test
  public void buildWithDefaultTypeResolverForListOfUnions() {
    ArrayList<Object> list = new ArrayList<>();
    list.add(new MyObject("1", "v"));
    list.add(new AnotherObject("2"));

    DataFetcher fetcher = env -> list;

    GraphQL graphQL =
        new SimpleGraphQLBuilder(schemaWithUnions).fetcher("Query", "list", fetcher).build();

    ExecutionResult response =
        graphQL.execute(
            "query { list { ... on MyObject { id value } ... on AnotherObject { id }} }");

    Map<String, String> map1 = new HashMap<>();
    map1.put("id", "1");
    map1.put("value", "v");

    Map<String, String> map2 = new HashMap<>();
    map2.put("id", "2");

    ArrayList<Map<String, String>> expectedList = new ArrayList<>();
    expectedList.add(map1);
    expectedList.add(map2);

    assertThat(response.getData(), is(expectedResponse("list", expectedList)));
  }

  @Test
  public void fetchingExceptionsHandled() throws RuntimeException {
    DataFetcher fetcher =
        env -> {
          throw new RuntimeException("oops");
        };

    GraphQL graphQL = new SimpleGraphQLBuilder(schema).fetcher("Query", "read", fetcher).build();

    ExecutionResult response = graphQL.execute("query { read }");
    assertErrorWithMessage(response);
  }

  @Test
  public void configureLowerLevel() throws RuntimeException {
    AtomicBoolean called = new AtomicBoolean(false);

    new SimpleGraphQLBuilder(schema)
        .configure(
            (TypeDefinitionRegistry registry, RuntimeWiring.Builder runtimeWiringBuilder) -> {
              called.set(true);
            })
        .build();

    assertThat(called.get(), is(true));
  }

  @Test
  public void callsCustomExceptionHandler() throws RuntimeException {
    DataFetcher fetcher =
        env -> {
          throw new RuntimeException("oops");
        };
    TestExceptionHandler exceptionHandler = new TestExceptionHandler();

    GraphQL graphQL =
        new SimpleGraphQLBuilder(schema)
            .fetcher("Query", "read", fetcher)
            .exceptionHandler(exceptionHandler)
            .build();

    ExecutionResult response = graphQL.execute("query { read }");
    assertErrorWithMessage(response);
    assertTrue(exceptionHandler.called);
  }

  @Test
  public void timeScalarsAreLong() {
    final Reader schema =
        new StringReader(
            "scalar EpochMilliseconds "
                + "scalar EpochSeconds "
                + "scalar Milliseconds "
                + "scalar Seconds "
                + "scalar Minutes "
                + "type Query { "
                + "    epochMilliseconds: EpochMilliseconds "
                + "    epochSeconds: EpochSeconds "
                + "    milliseconds: Milliseconds "
                + "    seconds: Seconds "
                + "    minutes: Minutes "
                + "}");

    GraphQL graphQL =
        new SimpleGraphQLBuilder(schema)
            .fetcher("Query", "epochMilliseconds", _env -> new EpochMilliseconds(2000L))
            .fetcher("Query", "epochSeconds", _env -> new EpochSeconds(2L))
            .fetcher("Query", "milliseconds", _env -> new Milliseconds(1000L))
            .fetcher("Query", "seconds", _env -> new Seconds(1L))
            .fetcher("Query", "minutes", _env -> new Minutes(60L))
            .build();

    String query = "query { epochMilliseconds epochSeconds milliseconds seconds minutes }";
    ExecutionResult response = graphQL.execute(query);

    HashMap<String, Long> expected = new HashMap<>();
    expected.put("epochMilliseconds", 2000L);
    expected.put("epochSeconds", 2L);
    expected.put("milliseconds", 1000L);
    expected.put("seconds", 1L);
    expected.put("minutes", 60L);

    assertThat(response.getData(), is(expected));
  }

  @Test
  public void disablingPredefinedScalarsFallsBackToStringCoercing() {
    final Reader schema =
        new StringReader(
            "scalar EpochMilliseconds "
                + "type Query { "
                + "    epochMilliseconds: EpochMilliseconds "
                + "}");

    GraphQL graphQL =
        new SimpleGraphQLBuilder(schema)
            .fetcher("Query", "epochMilliseconds", _env -> new EpochMilliseconds(2000L))
            .usePredefinedScalars(false)
            .build();

    String query = "query { epochMilliseconds }";
    ExecutionResult response = graphQL.execute(query);

    assertThat(
        response.getData(),
        is(expectedResponse("epochMilliseconds", new EpochMilliseconds(2000L).toString())));
  }

  @Test
  public void scalarDefaultsHaveDescriptions() {
    GraphQL graphQL = new SimpleGraphQLBuilder(schema).build();

    String query =
        "query IntrospectionQuery { "
            + "  __schema { "
            + "    types { "
            + "      name "
            + "      description "
            + "    } "
            + "  } "
            + "}";

    ExecutionResult response = graphQL.execute(query);
    Map<String, Map<String, List<Map<String, String>>>> datum = response.getData();
    List<Map<String, String>> types = datum.get("__schema").get("types");

    HashMap<String, String> stringyDescription = new HashMap<>();
    stringyDescription.put("name", "Stringy");
    stringyDescription.put("description", "Stringy description");
    assertThat(types, hasItem(stringyDescription));

    HashMap<String, String> numberyDescription = new HashMap<>();
    numberyDescription.put("name", "Numbery");
    numberyDescription.put("description", null);
    assertThat(types, hasItem(numberyDescription));
  }

  @Test
  public void scalarsRegisteredKeepDescriptions() {
    GraphQLScalarType stringy =
        GraphQLScalarType.newScalar().name("Stringy").coercing(new GraphqlStringCoercing()).build();

    GraphQL graphQL = new SimpleGraphQLBuilder(schema).scalar(stringy.getName(), stringy).build();

    String query =
        "query IntrospectionQuery { "
            + "  __schema { "
            + "    types { "
            + "      name "
            + "      description "
            + "    } "
            + "  } "
            + "}";

    ExecutionResult response = graphQL.execute(query);
    Map<String, Map<String, List<Map<String, String>>>> datum = response.getData();
    List<Map<String, String>> types = datum.get("__schema").get("types");

    HashMap<String, String> stringyDescription = new HashMap<>();
    stringyDescription.put("name", "Stringy");
    stringyDescription.put("description", "Stringy description");
    assertThat(types, hasItem(stringyDescription));
  }

  private void assertErrorWithMessage(ExecutionResult response) {
    assertThat(response.getErrors().size(), is(1));

    ExceptionWhileDataFetching exception = (ExceptionWhileDataFetching) response.getErrors().get(0);
    assertThat(exception.getMessage(), endsWith("oops"));
  }

  static class TestExceptionHandler extends SimpleDataFetcherExceptionHandler {
    public boolean called = false;

    @Override
    public DataFetcherExceptionHandlerResult onException(
        DataFetcherExceptionHandlerParameters handlerParameters) {
      called = true;
      return super.onException(handlerParameters);
    }
  }

  public static class MyObject {
    private final String id;
    private final String value;

    public MyObject(String id, String value) {
      this.id = id;
      this.value = value;
    }

    public String getId() {
      return id;
    }

    public String getValue() {
      return value;
    }
  }

  public static class AnotherObject {
    private final String id;

    public AnotherObject(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }
  }

  private <T> Map<String, T> expectedResponse(String key, T value) {
    Map<String, T> expected = new HashMap<>();
    expected.put(key, value);
    return expected;
  }
}
