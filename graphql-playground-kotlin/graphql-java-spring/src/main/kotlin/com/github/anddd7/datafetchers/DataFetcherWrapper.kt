package com.github.anddd7.datafetchers

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
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
