package com.github.anddd7.datafetchers

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
  private val log: Logger
    get() = LoggerFactory.getLogger(this.javaClass)

  suspend fun fetch(environment: DataFetchingEnvironment): T

  override fun get(environment: DataFetchingEnvironment): CompletableFuture<T> {
    log.info("build completable future")

    val future = CompletableFuture.supplyAsync {
      log.info("trigger run blocking")

      val result = runBlocking(Dispatchers.Default) {
        log.info("call fetching in coroutine")

        val fetch = fetch(environment)

        log.info("got result of fetching")

        fetch
      }

      log.info("finished run blocking")

      result
    }

    log.info("return completable future")

    return future
  }
}
