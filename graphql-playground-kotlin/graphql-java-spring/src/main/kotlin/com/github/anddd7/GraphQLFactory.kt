package com.github.anddd7

import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser

class GraphQLFactory {
  fun build(graphQLDefinition: String, fetchers: List<DataFetcherWrapper<*>>): GraphQL {
    val typeRegistry = getTypeDefinitionRegistry(graphQLDefinition)
    val runtimeWiring = getRuntimeWiring(fetchers)
    val graphQLSchema = SchemaGenerator().makeExecutableSchema(typeRegistry, runtimeWiring)

    return GraphQL.newGraphQL(graphQLSchema)
//        .queryExecutionStrategy(AsyncExecutionStrategy())
//        .mutationExecutionStrategy(AsyncSerialExecutionStrategy())
        .build()
  }

  private fun getTypeDefinitionRegistry(graphQLDefinition: String) =
      SchemaParser().parse(graphQLDefinition)

  private fun getRuntimeWiring(fetchers: List<DataFetcherWrapper<*>>): RuntimeWiring =
      newRuntimeWiring().apply {
        fetchers.forEach { fetcher ->
          type(fetcher.getType()) { it.dataFetcher(fetcher.getFieldName(), fetcher) }
        }
      }.build()
}
