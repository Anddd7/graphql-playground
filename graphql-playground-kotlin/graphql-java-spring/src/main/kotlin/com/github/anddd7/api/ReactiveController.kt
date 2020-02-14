package com.github.anddd7.api

import com.github.anddd7.factory.CoroutineDataFetcherWrapper
import com.github.anddd7.factory.DataFetcherWrapper
import com.github.anddd7.factory.FluxDataFetcherWrapper
import com.github.anddd7.factory.FutureDataFetcherWrapper
import com.github.anddd7.factory.MonoDataFetcherWrapper
import com.github.anddd7.entity.Author
import com.github.anddd7.entity.AuthorRepository
import com.github.anddd7.entity.Book
import com.github.anddd7.entity.BookRepository
import com.github.anddd7.factory.GraphQLFactory
import graphql.ExecutionInput.newExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.supplyAsync
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@RestController
@RequestMapping("/reactive/graphql")
class ReactiveController(
    private val graphQLFactory: GraphQLFactory
) {
  @Value("classpath:schema.graphqls")
  private lateinit var schemaDefinition: Resource

  private val graphQLEngines: ConcurrentMap<Scenario, GraphQL> = ConcurrentHashMap<Scenario, GraphQL>()

  @RequestMapping("/future")
  fun future(@RequestBody request: GraphQLRequest) = process(request, Scenario.Future)

  @RequestMapping("/mono")
  fun mono(@RequestBody request: GraphQLRequest) = process(request, Scenario.Reactor)


  @RequestMapping("/coroutines")
  fun coroutines(@RequestBody request: GraphQLRequest) = process(request, Scenario.Coroutines)

  private fun process(request: GraphQLRequest, scenario: Scenario): Mono<Map<String, Any>> {
    val graphQL = graphQLEngines.getOrPut(scenario) {
      graphQLFactory.graphQL(schemaDefinition, scenario.dataFetchers())
    }
    val input = newExecutionInput()
        .query(request.query)
        .variables(request.variables)
        .build()

    val result = graphQL.executeAsync(input)

    return result.toMono().map(ExecutionResult::toSpecification)
  }
}

enum class Scenario {
  Future, Reactor, Coroutines;

  fun dataFetchers(): List<DataFetcherWrapper<*>> =
      when (this) {
        Future -> listOf(
            object : FutureDataFetcherWrapper<Book> {
              override fun getType() = "Query"
              override fun getFieldName() = "bookById"
              override fun get(environment: DataFetchingEnvironment): CompletableFuture<Book> {
                val id = environment.getArgument<String>("id").toInt()
                return supplyAsync { BookRepository.findById(id) }
              }
            },
            object : FutureDataFetcherWrapper<List<Book>> {
              override fun getType() = "Query"
              override fun getFieldName() = "books"
              override fun get(environment: DataFetchingEnvironment): CompletableFuture<List<Book>> {
                return supplyAsync(BookRepository::findAll)
              }
            },
            object : FutureDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "author"
              override fun get(environment: DataFetchingEnvironment): CompletableFuture<Author> {
                val id = environment.getSource<Book>().authorId
                return supplyAsync { AuthorRepository.findById(id) }
              }
            },
            object : FutureDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "editor"
              override fun get(environment: DataFetchingEnvironment): CompletableFuture<Author> {
                val id = environment.getSource<Book>().editorId
                return supplyAsync { AuthorRepository.findById(id) }
              }
            }
        )
        Reactor -> listOf(
            object : MonoDataFetcherWrapper<Book> {
              override fun getType() = "Query"
              override fun getFieldName() = "bookById"
              override fun fetch(environment: DataFetchingEnvironment): reactor.core.publisher.Mono<Book> {
                val id = environment.getArgument<String>("id").toInt()
                return BookRepository.reactorFindById(id)
              }
            },
            object : FluxDataFetcherWrapper<Book> {
              override fun getType() = "Query"
              override fun getFieldName() = "books"
              override fun fetch(environment: DataFetchingEnvironment): Flux<Book> {
                return BookRepository.reactorFindAll()
              }
            },
            object : MonoDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "author"
              override fun fetch(environment: DataFetchingEnvironment): reactor.core.publisher.Mono<Author> {
                val id = environment.getSource<Book>().authorId
                return AuthorRepository.reactorFindById(id)
              }
            },
            object : MonoDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "editor"
              override fun fetch(environment: DataFetchingEnvironment): reactor.core.publisher.Mono<Author> {
                val id = environment.getSource<Book>().editorId
                return AuthorRepository.reactorFindById(id)
              }
            }
        )
        Coroutines -> listOf(
            object : CoroutineDataFetcherWrapper<Book> {
              override fun getType() = "Query"
              override fun getFieldName() = "bookById"
              override suspend fun fetch(environment: DataFetchingEnvironment): Book {
                val id = environment.getArgument<String>("id").toInt()
                return BookRepository.coFindById(id)
              }
            },
            object : CoroutineDataFetcherWrapper<List<Book>> {
              override fun getType() = "Query"
              override fun getFieldName() = "books"
              override suspend fun fetch(environment: DataFetchingEnvironment): List<Book> {
                return BookRepository.coFindAll()
              }
            },
            object : CoroutineDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "author"
              override suspend fun fetch(environment: DataFetchingEnvironment): Author {
                val id = environment.getSource<Book>().authorId

                return AuthorRepository.coFindById(id)
              }
            },
            object : CoroutineDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "editor"
              override suspend fun fetch(environment: DataFetchingEnvironment): Author {
                val id = environment.getSource<Book>().editorId
                return AuthorRepository.coFindById(id)
              }
            }
        )
      }
}
