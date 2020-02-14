package com.github.anddd7.graphql

import graphql.GraphQL
import graphql.schema.DataFetcher
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.nio.file.Files.readAllBytes
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.memberFunctions

@Component
class KotlinGraphQLFactory {
  fun graphQL(schemaDefinition: Resource, fetchers: List<KotlinFetcher>): GraphQL {
    val definition = String(readAllBytes(schemaDefinition.file.toPath()))

    return buildGraphQL(definition, fetchers)
  }

  companion object {

    fun buildGraphQL(definition: String, fetchers: List<KotlinFetcher>): GraphQL {
      val typeRegistry = SchemaParser().parse(definition)
      val runtimeWiring = getRuntimeWiring(fetchers)
      val graphQLSchema = SchemaGenerator().makeExecutableSchema(typeRegistry, runtimeWiring)

      return GraphQL.newGraphQL(graphQLSchema).build()
    }

    private fun getRuntimeWiring(fetchers: List<KotlinFetcher>): RuntimeWiring {
      val builder = newRuntimeWiring()
      val fetcherBuilders = fetchers.flatMap(Companion::buildDataFetchers)

      for (fetcher in fetcherBuilders) {
        builder.type(fetcher.first) { it.dataFetcher(fetcher.second, fetcher.third) }
      }

      return builder.build()
    }

    private val ignoreFunctions = listOf("hashCode", "equals", "toString")

    private fun buildDataFetchers(fetcher: KotlinFetcher): List<Triple<String, String, DataFetcher<out Any?>>> {
      val kClass = fetcher::class
      val kFunctions = getQueryFunctions(kClass)
      val typeName = getTypeName(fetcher, kClass)

      return kFunctions.map { fn ->
        Triple(
            typeName,
            fn.name,
            if (fn.isSuspend) {
              suspendDataFetcher(fn, fetcher)
            } else {
              blockingDataFetcher(fn, fetcher)
            }
        )
      }
    }

    private fun getQueryFunctions(kClass: KClass<out KotlinFetcher>) =
        kClass.memberFunctions.filterNot { it.name in ignoreFunctions }

    private fun getTypeName(fetcher: KotlinFetcher, kClass: KClass<out KotlinFetcher>) =
        if (fetcher is KotlinQuery) "Query" else kClass.simpleName!!.removeSuffix("DataFetcher")

    private fun blockingDataFetcher(fn: KFunction<*>, fetcher: KotlinFetcher): DataFetcher<Any?> =
        DataFetcher { environment ->
          fn.call(fetcher, environment)
        }

    private fun suspendDataFetcher(fn: KFunction<*>, fetcher: KotlinFetcher): DataFetcher<CompletableFuture<Any?>> =
        DataFetcher {
          GlobalScope.future { fn.callSuspend(fetcher, it) }
        }
  }
}
