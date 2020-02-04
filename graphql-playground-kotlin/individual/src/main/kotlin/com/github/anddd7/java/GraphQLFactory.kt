package com.github.anddd7.java

import graphql.GraphQL
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser

class GraphQLFactory {
  fun build(schema: String, type: String, field: String, dataFetcher: StaticDataFetcher): GraphQL {
    val typeDefinitionRegistry = SchemaParser().parse(schema)

    val runtimeWiring = newRuntimeWiring()
        .type(type) { it.dataFetcher(field, dataFetcher) }
        .build()

    val graphQLSchema = SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)

    return GraphQL.newGraphQL(graphQLSchema).build()
  }
}
