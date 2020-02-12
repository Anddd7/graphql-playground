package com.github.anddd7.api

import com.github.anddd7.graphql.DataFetcherWrapper
import com.github.anddd7.entity.Author
import com.github.anddd7.entity.AuthorRepository
import com.github.anddd7.entity.Book
import com.github.anddd7.entity.BookRepository
import com.github.anddd7.factory.GraphQLFactory
import graphql.ExecutionInput.newExecutionInput
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/mvc/graphql")
class MvcController(
    private val graphQLFactory: GraphQLFactory
) {
  @Value("classpath:schema.graphqls")
  private lateinit var schemaDefinition: Resource

  @RequestMapping("/all-good")
  fun allGood(@RequestBody request: GraphQLRequest): Map<String, Any> {
    val dataFetchers = listOf(
        object : DataFetcherWrapper<Book> {
          override fun getType() = "Query"
          override fun getFieldName() = "bookById"
          override fun get(environment: DataFetchingEnvironment) =
              BookRepository.findById(environment.getArgument<String>("id").toInt())
        },
        object : DataFetcherWrapper<Author> {
          override fun getType() = "Book"
          override fun getFieldName() = "author"
          override fun get(environment: DataFetchingEnvironment) =
              AuthorRepository.findById(environment.getSource<Book>().authorId)
        }
    )

    return process(request, dataFetchers)
  }

  @RequestMapping("/partial-error")
  fun partialError(@RequestBody request: GraphQLRequest): Map<String, Any> {
    val dataFetchers = listOf(
        object : DataFetcherWrapper<Book> {
          override fun getType() = "Query"
          override fun getFieldName() = "bookById"
          override fun get(environment: DataFetchingEnvironment) =
              BookRepository.findById(environment.getArgument<String>("id").toInt())
        },
        object : DataFetcherWrapper<Author> {
          override fun getType() = "Book"
          override fun getFieldName() = "author"
          override fun get(environment: DataFetchingEnvironment) =
              throw RuntimeException("Got exception while fetching data")
        }
    )

    return process(request, dataFetchers)
  }

  private fun process(request: GraphQLRequest, dataFetchers: List<DataFetcherWrapper<*>>): Map<String, Any> {
    val graphQL = graphQLFactory.graphQL(schemaDefinition, dataFetchers)
    val input = newExecutionInput()
        .query(request.query)
        .variables(request.variables)
        .build()

    return input.let(graphQL::execute).toSpecification()
  }
}
