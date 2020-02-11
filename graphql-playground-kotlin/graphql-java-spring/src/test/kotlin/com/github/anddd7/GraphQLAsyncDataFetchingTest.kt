package com.github.anddd7

import com.github.anddd7.datafetchers.CoroutineDataFetcherWrapper
import com.github.anddd7.datafetchers.DataFetcherWrapper
import com.github.anddd7.datafetchers.FluxDataFetcherWrapper
import com.github.anddd7.datafetchers.FutureDataFetcherWrapper
import com.github.anddd7.datafetchers.MonoDataFetcherWrapper
import com.github.anddd7.entity.Author
import com.github.anddd7.entity.AuthorRepository
import com.github.anddd7.entity.Book
import com.github.anddd7.entity.BookRepository
import graphql.ExecutionInput.newExecutionInput
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.RepeatedTest
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux.fromStream
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.just
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.supplyAsync

class GraphQLAsyncDataFetchingTest {
  private val log = LoggerFactory.getLogger(this.javaClass)

  private val delayTime: Long = 100

  @RepeatedTest(5)
  fun `should execute async query with future data fetchers`() {
    asyncFetching(
        listOf(
            object : FutureDataFetcherWrapper<Book> {
              override fun getType() = "Query"
              override fun getFieldName() = "bookById"
              override fun get(environment: DataFetchingEnvironment): CompletableFuture<Book> {
                log.info("[book] fire data fetching")

                return supplyAsync {
                  log.info("[book] get argument")

                  environment.getArgument<String>("id").toInt()
                }
                    .thenApplyAsync {
                      log.info("[book] waiting sleep")

                      Thread.sleep(delayTime)

                      log.info("[book] find by id")

                      BookRepository.findById(it)
                    }
              }
            },
            object : FutureDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "author"
              override fun get(environment: DataFetchingEnvironment): CompletableFuture<Author> {
                log.info("[author] fire data fetching")

                return supplyAsync {
                  log.info("[author] get argument")

                  environment.getSource<Book>().authorId
                }
                    .thenApplyAsync {
                      log.info("[author] waiting sleep")

                      Thread.sleep(delayTime)

                      log.info("[author] find by id")

                      AuthorRepository.findById(it)
                    }
              }
            },
            object : FutureDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "editor"
              override fun get(environment: DataFetchingEnvironment): CompletableFuture<Author> {
                log.info("[editor] fire data fetching")

                return supplyAsync {
                  log.info("[editor] get argument")

                  environment.getSource<Book>().editorId
                }
                    .thenApplyAsync {
                      log.info("[editor] waiting sleep")

                      Thread.sleep(delayTime)

                      log.info("[editor] find by id")

                      AuthorRepository.findById(it)
                    }
              }
            }
        )
    )
  }

  @RepeatedTest(5)
  fun `should execute async query with mono data fetchers`() {
    asyncFetching(
        listOf(
            object : MonoDataFetcherWrapper<Book> {
              override fun getType() = "Query"
              override fun getFieldName() = "bookById"
              override fun fetch(environment: DataFetchingEnvironment): Mono<Book> {
                log.info("[book] fire data fetching")

                return just(environment.getArgument<String>("id").toInt())
                    .delayElement(Duration.ofMillis(delayTime))
                    .map(BookRepository::findById)
                    .log()
              }
            },
            object : MonoDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "author"
              override fun fetch(environment: DataFetchingEnvironment): Mono<Author> {
                log.info("[author] fire data fetching")

                return just(environment.getSource<Book>().authorId)
                    .delayElement(Duration.ofMillis(delayTime))
                    .map(AuthorRepository::findById)
                    .log()
              }
            },
            object : MonoDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "editor"
              override fun fetch(environment: DataFetchingEnvironment): Mono<Author> {
                log.info("[editor] fire data fetching")

                return just(environment.getSource<Book>().editorId)
                    .delayElement(Duration.ofMillis(delayTime))
                    .map(AuthorRepository::findById)
                    .log()
              }
            }
        )
    )
  }

  @RepeatedTest(5)
  fun `should execute async query with kotlin coroutines`() {
    asyncFetching(
        listOf(
            object : CoroutineDataFetcherWrapper<Book> {
              override fun getType() = "Query"
              override fun getFieldName() = "bookById"
              override suspend fun fetch(environment: DataFetchingEnvironment): Book {
                log.info("[book] fire data fetching")

                log.info("[book] waiting sleep")

                delay(delayTime)

                log.info("[book] find by id")

                return BookRepository.findById(environment.getArgument<String>("id").toInt())
              }
            },
            object : CoroutineDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "author"
              override suspend fun fetch(environment: DataFetchingEnvironment): Author {
                log.info("[author] fire data fetching")

                log.info("[author] waiting sleep")

                delay(delayTime)

                log.info("[author] find by id")

                return AuthorRepository.findById(environment.getSource<Book>().authorId)
              }
            },
            object : CoroutineDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "editor"
              override suspend fun fetch(environment: DataFetchingEnvironment): Author {
                log.info("[editor] fire data fetching")

                log.info("[editor] waiting sleep")

                delay(delayTime)

                log.info("[editor] find by id")

                return AuthorRepository.findById(environment.getSource<Book>().editorId)
              }
            }
        )
    )
  }

  @RepeatedTest(5)
  fun `should execute async query with flux data fetchers`() {
    asyncFetchingList(
        listOf(
            object : FluxDataFetcherWrapper<Book> {
              override fun getType() = "Query"
              override fun getFieldName() = "books"
              override fun fetch(environment: DataFetchingEnvironment) =
                  fromStream { BookRepository.findAll().stream() }
                      .delayElements(Duration.ofMillis(delayTime))
            },
            object : MonoDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "author"
              override fun fetch(environment: DataFetchingEnvironment): Mono<Author> {
                log.info("[author] fire data fetching")

                return just(environment.getSource<Book>().authorId)
                    .delayElement(Duration.ofMillis(delayTime))
                    .map(AuthorRepository::findById)
                    .log()
              }
            },
            object : MonoDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "editor"
              override fun fetch(environment: DataFetchingEnvironment): Mono<Author> {
                log.info("[editor] fire data fetching")

                return just(environment.getSource<Book>().editorId)
                    .delayElement(Duration.ofMillis(delayTime))
                    .map(AuthorRepository::findById)
                    .log()
              }
            }
        )
    )
  }

  private fun asyncFetching(fetchers: List<DataFetcherWrapper<*>>) {
    val query = "{bookById(id: 1) {id,name,title,pageCount,author{firstName,lastName},editor{firstName,lastName},company{name,address}}}"
    val graphQL = build(fetchers)

    val async = newExecutionInput()
        .query(query)
        .build()
        .let(graphQL::executeAsync)
        .thenApplyAsync {
          it.getData<Map<String, Any>>()
        }
        .whenCompleteAsync { result, _ ->
          log.info("Finished with : $result")
        }

    log.info("Waiting for execution")

    // wait for async job
    val data = async.join()

    log.info("fetched data")

    val bookById = data["bookById"] as? Map<*, *> ?: emptyMap<String, Any>()

    assertThat(bookById["name"]).isEqualTo("Harry Potter and the Philosopher's Stone")
    assertThat(bookById["title"]).isEqualTo("Harry Potter and the Philosopher's Stone")
    assertThat(bookById["pageCount"]).isEqualTo(223)
  }

  private fun asyncFetchingList(fetchers: List<DataFetcherWrapper<*>>) {
    val query = "{books {id,name,title,pageCount,author{firstName,lastName},editor{firstName,lastName},company{name,address}}}"
    val graphQL = build(fetchers)

    val async = newExecutionInput()
        .query(query)
        .build()
        .let(graphQL::executeAsync)
        .thenApplyAsync {
          it.getData<Map<String, Any>>()
        }
        .whenCompleteAsync { result, _ ->
          log.info("Finished with : $result")
        }

    log.info("Waiting for execution")

    // wait for async job
    val data = async.join()

    log.info("fetched data")

    val books = data["books"] as? List<Map<*, *>> ?: emptyList()

    assertThat(books).hasSize(3)
  }
}
