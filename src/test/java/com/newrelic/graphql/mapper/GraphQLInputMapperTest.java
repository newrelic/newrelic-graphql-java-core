package com.newrelic.graphql.mapper;

import static org.junit.Assert.assertEquals;

import graphql.Scalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GraphQLInputMapperTest {

  private GraphQLInputMapper mapper;

  private GraphQLInputObjectType myObjectType =
      GraphQLInputObjectType.newInputObject().name("MyObject").build();

  private GraphQLEnumType myEnumType = GraphQLEnumType.newEnum().name("MyEnum").build();

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup() {
    String packageName = this.getClass().getPackage().getName();
    mapper = new GraphQLInputMapper(packageName);
  }

  @Test
  public void convertDoesNotTouchScalars() throws ClassNotFoundException {
    assertEquals("Howdy", mapper.convert("Howdy", Scalars.GraphQLString));
    assertEquals(Integer.valueOf(42), mapper.convert(42, Scalars.GraphQLInt));
    assertEquals(Double.valueOf(31.4), mapper.convert(31.4, Scalars.GraphQLFloat));
    assertEquals(true, mapper.convert(true, Scalars.GraphQLBoolean));
    assertEquals("", mapper.convert("", Scalars.GraphQLID));
    assertEquals(Long.valueOf(1000), mapper.convert((long) 1000, Scalars.GraphQLLong));
    assertEquals(Short.valueOf((short) 4), mapper.convert((short) 4, Scalars.GraphQLShort));
    assertEquals(Byte.valueOf((byte) 2), mapper.convert((byte) 2, Scalars.GraphQLByte));
    assertEquals(
        BigInteger.valueOf(2000),
        mapper.convert(BigInteger.valueOf(2000), Scalars.GraphQLBigInteger));
    assertEquals(
        BigDecimal.valueOf(3000),
        mapper.convert(BigDecimal.valueOf(3000), Scalars.GraphQLBigDecimal));
    assertEquals(Character.valueOf('j'), mapper.convert('j', Scalars.GraphQLChar));
  }

  @Test
  public void convertToEnum() throws ClassNotFoundException {
    MyEnum actual = mapper.convert("FIRST", myEnumType);
    assertEquals(MyEnum.FIRST, actual);
  }

  @Test
  public void convertToObject() throws ClassNotFoundException {
    Map<String, String> map = new HashMap<>();
    map.put("v1", "1");
    map.put("v2", "2");

    MyObject actual = mapper.convert(map, myObjectType);
    assertEquals(new MyObject("1", "2"), actual);
  }

  @Test
  public void convertWithNonNullableObject() throws ClassNotFoundException {
    Map<String, String> map = new HashMap<>();
    map.put("v1", "1");
    map.put("v2", "2");

    MyObject actual = mapper.convert(map, new GraphQLNonNull(myObjectType));
    assertEquals(new MyObject("1", "2"), actual);
  }

  @Test
  public void convertListOfObjects() throws ClassNotFoundException {
    Map<String, String> map = new HashMap<>();
    map.put("v1", "1");
    map.put("v2", "2");

    List<Map<String, String>> list = new ArrayList<>();
    list.add(map);

    List<MyObject> actual = mapper.convert(list, new GraphQLList(myObjectType));

    ArrayList<MyObject> expected = new ArrayList<>();
    expected.add(new MyObject("1", "2"));
    assertEquals(expected, actual);
  }

  @Test
  public void convertListOfNonNullObjects() throws ClassNotFoundException {
    Map<String, String> map = new HashMap<>();
    map.put("v1", "1");
    map.put("v2", "2");

    List<Map<String, String>> list = new ArrayList<>();
    list.add(map);

    List<MyObject> actual = mapper.convert(list, new GraphQLList(new GraphQLNonNull(myObjectType)));

    ArrayList<MyObject> expected = new ArrayList<>();
    expected.add(new MyObject("1", "2"));
    assertEquals(expected, actual);
  }

  @Test
  public void convertNonNullListOfNonNullObjects() throws ClassNotFoundException {
    Map<String, String> map = new HashMap<>();
    map.put("v1", "1");
    map.put("v2", "2");

    List<Map<String, String>> list = new ArrayList<>();
    list.add(map);

    List<MyObject> actual =
        mapper.convert(list, new GraphQLNonNull(new GraphQLList(new GraphQLNonNull(myObjectType))));

    ArrayList<MyObject> expected = new ArrayList<>();
    expected.add(new MyObject("1", "2"));
    assertEquals(expected, actual);
  }

  @Test
  public void convertListOfScalars() throws ClassNotFoundException {
    List<String> list = new ArrayList<>();
    list.add("hi");
    list.add("there");

    List<String> actual = mapper.convert(list, new GraphQLList(Scalars.GraphQLString));

    assertEquals(list, actual);
  }

  @Test
  public void convertNestedListOfObjects() throws ClassNotFoundException {
    Map<String, String> map = new HashMap<>();
    map.put("v1", "1");
    map.put("v2", "2");

    List<Map<String, String>> inner = new ArrayList<>();
    inner.add(map);

    List<List<Map<String, String>>> raw = new ArrayList<>();
    raw.add(inner);

    List<List<MyObject>> actual =
        mapper.convert(raw, new GraphQLList(new GraphQLList(myObjectType)));

    ArrayList<MyObject> expectedInner = new ArrayList<>();
    expectedInner.add(new MyObject("1", "2"));

    ArrayList<List<MyObject>> expected = new ArrayList<>();
    expected.add(expectedInner);

    assertEquals(expected, actual);
  }

  @Test
  public void convertNestedListOfScalars() throws ClassNotFoundException {
    List<String> inner = new ArrayList<>();
    inner.add("hi");
    inner.add("there");

    List<List<String>> list = new ArrayList<>();
    list.add(inner);

    List<List<String>> actual =
        mapper.convert(list, new GraphQLList(new GraphQLList(Scalars.GraphQLString)));

    assertEquals(list, actual);
  }

  @Test
  public void wrench() throws ClassNotFoundException {
    GraphQLDirective unsupportedGraphQLType = GraphQLDirective.newDirective().name("bunk").build();

    thrown.expect(ClassCastException.class);

    // If we don't assign the value the code gets skipped so it's not actually unused!!
    String failure = mapper.convert(new Integer(42), unsupportedGraphQLType);
  }
}
