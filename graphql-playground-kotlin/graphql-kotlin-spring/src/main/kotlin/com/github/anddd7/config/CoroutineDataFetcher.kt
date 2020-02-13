package com.github.anddd7.config

import graphql.schema.DataFetchingEnvironment
import kotlin.reflect.KClass

interface CoroutineDataFetcher<T : Any> {
  fun type(): KClass<T>
  suspend fun fetch(environment: DataFetchingEnvironment): T
}
