package com.github.anddd7.java

import graphql.GraphQL
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser

class GraphQLFactory {
  fun execute(): Map<Any, Any> {
    val schema = "type Query{hello: String}"

    val typeDefinitionRegistry = SchemaParser().parse(schema)

    val runtimeWiring = newRuntimeWiring()
        .type("Query") {
          it.dataFetcher("hello", StaticDataFetcher("world"))
        }
        .build()

    val graphQLSchema = SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)

    val build = GraphQL.newGraphQL(graphQLSchema).build()
    val executionResult = build.execute("{hello}")
    return executionResult.getData<Map<Any, Any>>()
  }
}
