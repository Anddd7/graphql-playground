package com.github.anddd7

import com.github.anddd7.factory.CoroutineDataFetcherWrapper
import com.github.anddd7.factory.DataFetcherWrapper
import com.github.anddd7.factory.FluxDataFetcherWrapper
import com.github.anddd7.factory.FutureDataFetcherWrapper
import com.github.anddd7.factory.MonoDataFetcherWrapper
import com.github.anddd7.entity.Author
import com.github.anddd7.entity.AuthorRepository
import com.github.anddd7.entity.Book
import com.github.anddd7.entity.BookRepository
import graphql.ExecutionInput.newExecutionInput
import graphql.schema.DataFetchingEnvironment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.RepeatedTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.supplyAsync

class GraphQLAsyncDataFetchingTest {
  private val log = LoggerFactory.getLogger(this.javaClass)

  fun <T> Logger.wrap(title: String, f: () -> T): T {
    log.info("[START] $title")
    val t = f()
    log.info("[ END ] $title")
    return t
  }

  suspend fun <T> Logger.coWrap(title: String, f: suspend () -> T): T {
    log.info("[START] $title")
    val t = f()
    log.info("[ END ] $title")
    return t
  }

  /**
   * 所有的completable future会并发执行, 执行时长会受线程池大小影响
   *
   * e.g
   * - Query里的 bookById 和 books 是并发查询的
   * - author 和 editor 也是并发的
   * - 但是线程池有限, 部分任务会被阻塞
   */
  @RepeatedTest(5)
  fun `should execute async query with future data fetchers`() {
    asyncFetching(
        listOf(
            object : FutureDataFetcherWrapper<Book> {
              override fun getType() = "Query"
              override fun getFieldName() = "bookById"
              override fun get(environment: DataFetchingEnvironment): CompletableFuture<Book> {
                return log.wrap("[book] fire data fetching") {
                  supplyAsync(environment.getArgument<String>("id")::toInt)
                      .thenApplyAsync {
                        log.wrap("[book] find by id: $it") {
                          BookRepository.findById(it)
                        }
                      }
                }
              }
            },
            object : FutureDataFetcherWrapper<List<Book>> {
              override fun getType() = "Query"
              override fun getFieldName() = "books"
              override fun get(environment: DataFetchingEnvironment): CompletableFuture<List<Book>> {
                return log.wrap("[books] fire data fetching") {
                  supplyAsync {
                    log.wrap("[books] find all", BookRepository::findAll)
                  }
                }
              }
            },
            object : FutureDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "author"
              override fun get(environment: DataFetchingEnvironment): CompletableFuture<Author> {
                return log.wrap("[author] fire data fetching") {
                  supplyAsync(environment.getSource<Book>()::authorId)
                      .thenApplyAsync {
                        log.wrap("[author] find by id: $it") {
                          AuthorRepository.findById(it)
                        }
                      }
                }
              }
            },
            object : FutureDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "editor"
              override fun get(environment: DataFetchingEnvironment): CompletableFuture<Author> {
                return log.wrap("[editor] fire data fetching") {
                  supplyAsync(environment.getSource<Book>()::editorId)
                      .thenApplyAsync {
                        log.wrap("[editor] find by id: $it") {
                          AuthorRepository.findById(it)
                        }
                      }
                }
              }
            }
        )
    )
  }

  /**
   * 同completable future, 只是mono用了另外的线程池, 因此执行时间可能和future不同
   */
  @RepeatedTest(5)
  fun `should execute async query with reactor data fetchers`() {
    asyncFetching(
        listOf(
            object : MonoDataFetcherWrapper<Book> {
              override fun getType() = "Query"
              override fun getFieldName() = "bookById"
              override fun fetch(environment: DataFetchingEnvironment): Mono<Book> {
                val id = environment.getArgument<String>("id").toInt()

                return log.wrap("[book] fire data fetching: $id") {
                  BookRepository.reactorFindById(id).log()
                }
              }
            },
            object : FluxDataFetcherWrapper<Book> {
              override fun getType() = "Query"
              override fun getFieldName() = "books"
              override fun fetch(environment: DataFetchingEnvironment): Flux<Book> {
                return log.wrap("[books] fire data fetching") {
                  BookRepository.reactorFindAll().log()
                }
              }
            },
            object : MonoDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "author"
              override fun fetch(environment: DataFetchingEnvironment): Mono<Author> {
                val id = environment.getSource<Book>().authorId

                return log.wrap("[author] fire data fetching: $id") {
                  AuthorRepository.reactorFindById(id).log()
                }
              }
            },
            object : MonoDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "editor"
              override fun fetch(environment: DataFetchingEnvironment): Mono<Author> {
                val id = environment.getSource<Book>().editorId

                return log.wrap("[editor] fire data fetching: $id") {
                  AuthorRepository.reactorFindById(id).log()
                }
              }
            }
        )
    )
  }

  /**
   * 同理, 只是线程池不同
   *
   * @see CoroutineDataFetcherWrapper
   */
  @RepeatedTest(5)
  fun `should execute async query with kotlin coroutines`() {
    asyncFetching(
        listOf(
            object : CoroutineDataFetcherWrapper<Book> {
              override fun getType() = "Query"
              override fun getFieldName() = "bookById"
              override suspend fun fetch(environment: DataFetchingEnvironment): Book {
                val id = environment.getArgument<String>("id").toInt()

                return log.coWrap("[book] fire data fetching: $id") {
                  BookRepository.coFindById(id)
                }
              }
            },
            object : CoroutineDataFetcherWrapper<List<Book>> {
              override fun getType() = "Query"
              override fun getFieldName() = "books"
              override suspend fun fetch(environment: DataFetchingEnvironment): List<Book> {
                return log.coWrap("[books] fire data fetching") {
                  BookRepository.coFindAll()
                }
              }
            },
            object : CoroutineDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "author"
              override suspend fun fetch(environment: DataFetchingEnvironment): Author {
                val id = environment.getSource<Book>().authorId

                return log.coWrap("[author] fire data fetching: $id") {
                  AuthorRepository.coFindById(id)
                }
              }
            },
            object : CoroutineDataFetcherWrapper<Author> {
              override fun getType() = "Book"
              override fun getFieldName() = "editor"
              override suspend fun fetch(environment: DataFetchingEnvironment): Author {
                val id = environment.getSource<Book>().editorId

                return log.coWrap("[editor] fire data fetching: $id") {
                  AuthorRepository.coFindById(id)
                }
              }
            }
        )
    )
  }

  private fun asyncFetching(fetchers: List<DataFetcherWrapper<*>>) {
    val bookByIdQuery = "bookById(id: 1) {id,name,title,pageCount,author{firstName,lastName},editor{firstName,lastName},company{name,address}}"
    val booksQuery = "books {id,name,title,pageCount,author{firstName,lastName},editor{firstName,lastName},company{name,address}}"
    val query = "{$bookByIdQuery,$booksQuery}"

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
    val books = data["books"] as? List<*> ?: emptyList<Any>()

    assertThat(bookById["name"]).isEqualTo("Harry Potter and the Philosopher's Stone")
    assertThat(bookById["title"]).isEqualTo("Harry Potter and the Philosopher's Stone")
    assertThat(bookById["pageCount"]).isEqualTo(223)

    assertThat(books).hasSize(3)
  }
}
