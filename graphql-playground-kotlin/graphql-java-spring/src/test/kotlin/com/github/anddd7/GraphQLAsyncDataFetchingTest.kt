package com.github.anddd7

import com.github.anddd7.datafetchers.DataFetcherWrapper
import com.github.anddd7.entity.Author
import com.github.anddd7.entity.AuthorRepository
import com.github.anddd7.entity.Book
import com.github.anddd7.entity.BookRepository
import graphql.ExecutionInput.newExecutionInput
import graphql.schema.DataFetchingEnvironment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.supplyAsync

class GraphQLAsyncDataFetchingTest {
  private val log = LoggerFactory.getLogger(this.javaClass)

  private val graphQLWithFuture = buildWithAsync()

  private fun buildWithAsync() = build(
      listOf(
          object : DataFetcherWrapper<CompletableFuture<Book>> {
            override fun getType() = "Query"
            override fun getFieldName() = "bookById"
            override fun get(environment: DataFetchingEnvironment) =
                supplyAsync { environment.getArgument<String>("id").toInt() }
                    .thenApplyAsync {
                      Thread.sleep(100)
                      BookRepository.findById(it)
                    }
          },
          object : DataFetcherWrapper<CompletableFuture<Author>> {
            override fun getType() = "Book"
            override fun getFieldName() = "author"
            override fun get(environment: DataFetchingEnvironment) =
                supplyAsync { environment.getSource<Book>().authorId }
                    .thenApplyAsync {
                      Thread.sleep(100)

                      AuthorRepository.findById(it)
                    }
          }
      )
  )

  @Test
  fun `should execute query with async way`() {
    val query = "{bookById(id: 1) {id,name,title,pageCount,author{firstName,lastName},company{name,address}}}"

    val async = newExecutionInput()
        .query(query)
        .build()
        .let(graphQLWithFuture::executeAsync)
        .thenApplyAsync {
          it.getData<Map<String, Any>>()
        }
        .whenCompleteAsync { result, _ ->
          log.info("Finished with : $result")
        }

    log.info("Waiting for execution")

    // wait for async job
    val data = async.join()

    val bookById = data["bookById"] as? Map<*, *> ?: emptyMap<String, Any>()

    assertThat(bookById["name"]).isEqualTo("Harry Potter and the Philosopher's Stone")
    assertThat(bookById["title"]).isEqualTo("Harry Potter and the Philosopher's Stone")
    assertThat(bookById["pageCount"]).isEqualTo(223)
  }
}
