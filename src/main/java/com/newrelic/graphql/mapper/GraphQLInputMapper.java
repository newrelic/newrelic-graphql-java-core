package com.newrelic.graphql.mapper;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;
import java.util.List;

/**
 * Primary class for mapping from primitive GraphQL inputs to custom classes.
 *
 * <p>This class uses Jackson, providing a structured way to convert incoming arguments in GraphQL
 * types to richer native classes.
 *
 * <pre>
 *   GraphQLInputMapper mapper = new GraphQLInputMapper("com.newrelic.my.model");
 *
 *   public T get(DataFetchingEnvironment environment) throws Exception {
 *     InputObject obj = mapper.convert(
 *                             environment.getArgument("inputObject"),
 *                             environment.getFieldDefinition().getArgument("inputObject").getType());
 *     //...
 *   }
 * </pre>
 */
public class GraphQLInputMapper {
  private final String packageName;
  private final ObjectMapper mapper;

  /** @param packageName Package name to find the destination type in for conversion */
  public GraphQLInputMapper(String packageName) {
    this(packageName, new ObjectMapper());
  }

  /**
   * @param packageName Package name to find the destination type in for conversion
   * @param mapper Custom Jackson ObjectMapper if additional configuration is required
   */
  public GraphQLInputMapper(String packageName, ObjectMapper mapper) {
    this.packageName = packageName;
    this.mapper = mapper;
  }

  /**
   * @param rawValue Incoming primitive value
   * @param graphQLType GraphQL type definition for the field value
   * @param <T> Desired type to convert to
   * @return Input object converted to the desired type
   * @throws ClassNotFoundException If requested type isn't found
   */
  public <T> T convert(Object rawValue, GraphQLType graphQLType) throws ClassNotFoundException {
    JavaType type = getType(graphQLType);
    if (type != null) {
      return mapper.convertValue(rawValue, type);
    }
    return cast(rawValue);
  }

  @SuppressWarnings("unchecked")
  private <T> T cast(Object rawValue) {
    return (T) rawValue;
  }

  private JavaType getType(GraphQLType type) throws ClassNotFoundException {
    if (type instanceof GraphQLList) {
      JavaType innerType = getType(((GraphQLList) type).getWrappedType());
      if (innerType != null) {
        return mapper.getTypeFactory().constructCollectionType(List.class, innerType);
      }
    } else if (type instanceof GraphQLInputObjectType) {
      return mapper.getTypeFactory().constructType(classInPackage(packageName, type));
    } else if (type instanceof GraphQLEnumType) {
      return mapper.getTypeFactory().constructType(classInPackage(packageName, type));
    } else if (type instanceof GraphQLNonNull) {
      return getType(((GraphQLNonNull) type).getWrappedType());
    }

    return null;
  }

  private Class<?> classInPackage(String packageName, GraphQLType type)
      throws ClassNotFoundException {
    String className = String.format("%s.%s", packageName, type.getName());
    return Class.forName(className);
  }
}
