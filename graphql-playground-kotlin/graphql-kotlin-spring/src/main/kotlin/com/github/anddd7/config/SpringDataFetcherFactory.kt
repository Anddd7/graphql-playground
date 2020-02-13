package com.github.anddd7.config

import com.expediagroup.graphql.execution.FunctionDataFetcher
import com.expediagroup.graphql.execution.KotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.extensions.deepName
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.schema.DataFetcher
import graphql.schema.DataFetcherFactory
import graphql.schema.PropertyDataFetcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
@Component
class CustomKotlinDataFetcherFactoryProvider(
    private val objectMapper: ObjectMapper,
    dataFetchers: List<CoroutineDataFetcher<*>>
) : KotlinDataFetcherFactoryProvider {

  private val dataFetchers =
      dataFetchers.map { it.type().simpleName to it }.toMap()

  override fun functionDataFetcherFactory(target: Any?, kFunction: KFunction<*>) =
      DataFetcherFactory<Any> {
        FunctionDataFetcher(
            target = target,
            fn = kFunction,
            objectMapper = objectMapper
        )
      }

  override fun propertyDataFetcherFactory(kClass: KClass<*>, kProperty: KProperty<*>): DataFetcherFactory<Any> =
      if (kProperty.isLateinit) {
        lateInitDataFetcherFactory()
      } else {
        defaultPropertyDataFetcherFactory(kProperty)
      }

  private fun defaultPropertyDataFetcherFactory(kProperty: KProperty<*>): DataFetcherFactory<Any> =
      DataFetcherFactory { PropertyDataFetcher<Any>(kProperty.name) }

  private fun lateInitDataFetcherFactory(): DataFetcherFactory<Any> =
      DataFetcherFactory<Any> {
        val typeName = it.fieldDefinition.type.deepName.removeSuffix("!")
        val fetcher = dataFetchers[typeName] ?: throw TODO("No such data fetcher")

        DataFetcher {
          GlobalScope.future { fetcher.fetch(it) }
        }
      }
}
