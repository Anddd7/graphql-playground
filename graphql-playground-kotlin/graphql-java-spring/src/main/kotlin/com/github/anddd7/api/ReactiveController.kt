package com.github.anddd7.api

import com.github.anddd7.datafetchers.DataFetcherWrapper
import com.github.anddd7.datafetchers.FutureDataFetcherWrapper
import com.github.anddd7.datafetchers.MonoDataFetcherWrapper
import com.github.anddd7.entity.Author
import com.github.anddd7.entity.AuthorRepository
import com.github.anddd7.entity.Book
import com.github.anddd7.entity.BookRepository
import com.github.anddd7.factory.GraphQLFactory
import graphql.ExecutionInput.newExecutionInput
import graphql.ExecutionResult
import graphql.schema.DataFetchingEnvironment
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.fromCallable
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.CompletableFuture.supplyAsync

@RestController
@RequestMapping("/reactive/graphql")
class ReactiveController(
    private val graphQLFactory: GraphQLFactory
) {
  private val log = LoggerFactory.getLogger(this.javaClass)

  @Value("classpath:schema.graphqls")
  private lateinit var schemaDefinition: Resource

  @RequestMapping("/future")
  fun future(@RequestBody request: GraphQLRequest): Mono<Map<String, Any>> {
    val dataFetchers = listOf(
        object : FutureDataFetcherWrapper<Book> {
          override fun getType() = "Query"
          override fun getFieldName() = "bookById"
          override fun get(environment: DataFetchingEnvironment) =
              supplyAsync(environment.getArgument<String>("id")::toInt)
                  .thenApplyAsync(BookRepository::findById)
        },
        object : FutureDataFetcherWrapper<Author> {
          override fun getType() = "Book"
          override fun getFieldName() = "author"
          override fun get(environment: DataFetchingEnvironment) =
              supplyAsync(environment.getSource<Book>()::authorId)
                  .thenApplyAsync(AuthorRepository::findById)
        }
    )

    return process(request, dataFetchers)
  }

  @RequestMapping("/mono")
  fun mono(@RequestBody request: GraphQLRequest): Mono<Map<String, Any>> {
    val dataFetchers = listOf(
        object : MonoDataFetcherWrapper<Book> {
          override fun getType() = "Query"
          override fun getFieldName() = "bookById"
          override fun fetch(environment: DataFetchingEnvironment) =
              fromCallable(environment.getArgument<String>("id")::toInt)
                  .map(BookRepository::findById)
                  .log()
        },
        object : MonoDataFetcherWrapper<Author> {
          override fun getType() = "Book"
          override fun getFieldName() = "author"
          override fun fetch(environment: DataFetchingEnvironment) =
              fromCallable(environment.getSource<Book>()::authorId)
                  .map(AuthorRepository::findById)
                  .log()
        }
    )

    return process(request, dataFetchers)
  }

  private fun process(request: GraphQLRequest, dataFetchers: List<DataFetcherWrapper<*>>): Mono<Map<String, Any>> {
    val graphQL = graphQLFactory.graphQL(schemaDefinition, dataFetchers)
    val input = newExecutionInput()
        .query(request.query)
        .variables(request.variables)
        .build()

    return input.let(graphQL::executeAsync).thenApplyAsync(ExecutionResult::toSpecification).toMono()
  }
}
