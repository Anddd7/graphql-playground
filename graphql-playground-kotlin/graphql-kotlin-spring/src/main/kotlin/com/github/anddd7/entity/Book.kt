package com.github.anddd7.entity

import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.delay
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalDateTime.of
import java.time.format.DateTimeFormatter.ofPattern

data class Book(
    val id: Int = 0,
    val name: String,
    val pageCount: Int,
    val authorId: Int,
    val editorId: Int,
    val company: Company,
    val publishedAt: LocalDateTime
) {
  fun getPublishedAt(environment: DataFetchingEnvironment) =
      ofPattern(environment.getArgument<String>("dateFormat")).let(publishedAt::format)
}

data class Company(
    val name: String,
    val address: String
)

@Repository
class BookRepository {
  private val github = Company("Github", "https://github.com")
  private val thoughtworks = Company("ThoughtWorks", "https://www.thoughtworks.com")

  private val books = listOf(
      Book(
          1, "Harry Potter and the Philosopher's Stone",
          223, 1, 1, github, of(2020, 1, 1, 0, 0, 0)
      ),
      Book(2, "Moby Dick",
          635, 2, 2, github, of(2020, 1, 1, 0, 0, 0)
      ),
      Book(3, "Interview with the vampire's Stone",
          371, 3, 3, thoughtworks, of(2020, 1, 1, 0, 0, 0)
      )
  )

  fun findById(id: Int): Book {
    Thread.sleep(100)

    return books.first { it.id == id }
  }

  fun reactorFindById(id: Int): Mono<Book> {
    return Mono
        .fromCallable { books.first { it.id == id } }
        .delayElement(Duration.ofMillis(100))
  }

  fun findAll(): List<Book> {
    Thread.sleep(100)

    return books
  }

  suspend fun coFindById(id: Int): Book {
    delay(100)

    return books.first { it.id == id }
  }

  fun reactorFindAll(): Flux<Book> {
    return Mono
        .delay(Duration.ofMillis(100))
        .flatMapMany { Flux.fromStream(books::stream) }
  }

  suspend fun coFindAll(): List<Book> {
    delay(100)

    return books
  }
}
