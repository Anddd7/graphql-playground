package com.github.anddd7.graphql.plugins

import com.expediagroup.graphql.execution.FunctionDataFetcher
import com.expediagroup.graphql.execution.KotlinDataFetcherFactoryProvider
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.schema.DataFetcherFactory
import graphql.schema.DataFetchingEnvironment
import graphql.schema.PropertyDataFetcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactor.asCoroutineContext
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

/**
 * Extend function data fetcher to apply graphql context as coroutine context
 */
@ExperimentalCoroutinesApi
@Component
class CustomKotlinDataFetcherFactoryProvider(
    private val objectMapper: ObjectMapper
) : KotlinDataFetcherFactoryProvider {
  override fun functionDataFetcherFactory(target: Any?, kFunction: KFunction<*>) =
      DataFetcherFactory {
        ContextualFunctionDataFetcher(target, kFunction, objectMapper)
      }

  override fun propertyDataFetcherFactory(kClass: KClass<*>, kProperty: KProperty<*>) =
      DataFetcherFactory<Any?> { PropertyDataFetcher(kProperty.name) }
}

@ExperimentalCoroutinesApi
class ContextualFunctionDataFetcher(
    private val target: Any?,
    private val fn: KFunction<*>,
    objectMapper: ObjectMapper
) : FunctionDataFetcher(target, fn, objectMapper) {
  override fun get(environment: DataFetchingEnvironment): Any? {
    val instance = target ?: environment.getSource() ?: return null
    val context = environment.getContext<QueryContext>()
    val parameterValues = getParameterValues(fn, environment)

    return if (fn.isSuspend) {
      runSuspendingFunction(
          instance,
          parameterValues,
          context.toReactorContext().asCoroutineContext()
      )
    } else {
      runBlockingFunction(instance, parameterValues)
    }
  }
}
