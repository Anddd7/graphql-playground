package com.github.anddd7.datafetchers

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors.toList

interface DataFetcherWrapper<T> : DataFetcher<T> {
  fun getType(): String
  fun getFieldName(): String
}

interface FutureDataFetcherWrapper<T> : DataFetcherWrapper<CompletableFuture<T>>

interface MonoDataFetcherWrapper<T> : FutureDataFetcherWrapper<T> {
  fun fetch(environment: DataFetchingEnvironment): Mono<T>

  override fun get(environment: DataFetchingEnvironment) =
      fetch(environment).toFuture()
}

interface FluxDataFetcherWrapper<T> : FutureDataFetcherWrapper<List<T>> {
  fun fetch(environment: DataFetchingEnvironment): Flux<T>

  override fun get(environment: DataFetchingEnvironment) =
      fetch(environment).collect(toList()).toFuture()
}

interface CoroutineDataFetcherWrapper<T> : DataFetcherWrapper<CompletableFuture<T>> {
  suspend fun fetch(environment: DataFetchingEnvironment): T

  override fun get(environment: DataFetchingEnvironment) = CompletableFuture.supplyAsync {
    runBlocking(Dispatchers.Default) {
      fetch(environment)
    }
  }
}
