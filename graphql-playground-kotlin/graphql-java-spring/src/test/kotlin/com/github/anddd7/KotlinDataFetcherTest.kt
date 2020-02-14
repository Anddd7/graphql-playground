package com.github.anddd7

import com.github.anddd7.entity.Author
import com.github.anddd7.entity.AuthorRepository
import com.github.anddd7.entity.Book
import com.github.anddd7.entity.BookRepository
import com.github.anddd7.graphql.KotlinDataFetcher
import com.github.anddd7.graphql.KotlinFetcher
import com.github.anddd7.graphql.KotlinQuery
import graphql.ExecutionInput
import graphql.schema.DataFetchingEnvironment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class KotlinFetcherTest {

  @Test
  fun `should build data fetchers of query`() {
    val fetchers = listOf(
        Query()
    )

    fetching(fetchers)
  }

  private fun fetching(fetchers: List<KotlinFetcher>) {
    val bookByIdQuery = "bookById(id: 1) {id}"
    val booksQuery = "books {id}"
    val query = "{$bookByIdQuery,$booksQuery}"

    val graphQL = buildKotlin(fetchers)

    val async = ExecutionInput.newExecutionInput()
        .query(query)
        .build()
        .let(graphQL::executeAsync)
        .thenApplyAsync { it.getData<Map<String, Any>>() }

    // wait for async job
    val data = async.join()

    val bookById = data["bookById"] as? Map<*, *> ?: emptyMap<String, Any>()
    val books = data["books"] as? List<*> ?: emptyList<Any>()

    assertThat(bookById["id"]).isNotNull
    assertThat(books).hasSize(3)
  }
}

class Query : KotlinQuery {
  suspend fun bookById(environment: DataFetchingEnvironment): Book =
      BookRepository.coFindById(environment.getArgument<String>("id").toInt())

  suspend fun books(environment: DataFetchingEnvironment): List<Book> = BookRepository.coFindAll()
}

class BookDataFetcher : KotlinDataFetcher {
  suspend fun author(environment: DataFetchingEnvironment): Author =
      AuthorRepository.coFindById(environment.getSource<Book>().authorId)

  suspend fun editor(environment: DataFetchingEnvironment):
      Author = AuthorRepository.coFindById(environment.getSource<Book>().editorId)
}
