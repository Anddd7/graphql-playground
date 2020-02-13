package com.github.anddd7.config

import com.expediagroup.graphql.execution.FunctionDataFetcher
import com.expediagroup.graphql.execution.KotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.extensions.deepName
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.schema.DataFetcher
import graphql.schema.DataFetcherFactory
import graphql.schema.PropertyDataFetcher
import org.springframework.beans.factory.BeanFactory
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
@Component
class CustomKotlinDataFetcherFactoryProvider(
    private val objectMapper: ObjectMapper,
    private val beanFactory: BeanFactory
) : KotlinDataFetcherFactoryProvider {
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
        val targetedTypeName = it
            .fieldDefinition?.type?.deepName
            ?.removeSuffix("!")
            ?.removeSuffix("DTO")
            ?.removeSuffix("Input")

        beanFactory.getBean("${targetedTypeName}DataFetcher") as DataFetcher<Any>
      }
}
