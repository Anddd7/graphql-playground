package com.github.anddd7.factory

import com.github.anddd7.datafetchers.DataFetcherWrapper
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

  fun graphQL(schemaDefinition: Resource, dataFetchers: List<DataFetcherWrapper<*>>): GraphQL {
    val definition = String(readAllBytes(schemaDefinition.file.toPath()))

    return buildGraphQL(definition, dataFetchers)
  }

  companion object {
    fun buildGraphQL(definition: String, dataFetchers: List<DataFetcherWrapper<*>>): GraphQL {
      val typeRegistry = SchemaParser().parse(definition)
      val runtimeWiring = getRuntimeWiring(dataFetchers)
      val graphQLSchema = SchemaGenerator().makeExecutableSchema(typeRegistry, runtimeWiring)

      return GraphQL.newGraphQL(graphQLSchema).build()
    }

    private fun getRuntimeWiring(reactiveMonoFetchers: List<DataFetcherWrapper<*>>): RuntimeWiring =
        newRuntimeWiring().apply {
          reactiveMonoFetchers.forEach { fetcher ->
            type(fetcher.getType()) { it.dataFetcher(fetcher.getFieldName(), fetcher) }
          }
        }.build()
  }
}

