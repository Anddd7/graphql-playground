package com.github.anddd7.factory

import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.nio.file.Files.readAllBytes

@Component
class GraphQLFactory {

  fun graphQL(schemaDefinition: Resource, fetchers: List<DataFetcherWrapper<*>>): GraphQL {
    val definition = String(readAllBytes(schemaDefinition.file.toPath()))

    return buildGraphQL(definition, fetchers)
  }

  companion object {
    fun buildGraphQL(definition: String, fetchers: List<DataFetcherWrapper<*>>): GraphQL {
      val typeRegistry = SchemaParser().parse(definition)
      val runtimeWiring = getRuntimeWiring(fetchers)
      val graphQLSchema = SchemaGenerator().makeExecutableSchema(typeRegistry, runtimeWiring)

      return GraphQL.newGraphQL(graphQLSchema).build()
    }

    private fun getRuntimeWiring(fetchers: List<DataFetcherWrapper<*>>): RuntimeWiring =
        newRuntimeWiring().apply {
          fetchers.forEach { fetcher ->
            type(fetcher.getType()) { it.dataFetcher(fetcher.getFieldName(), fetcher) }
          }
        }.build()
  }
}

