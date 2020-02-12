package com.github.anddd7.entity

import kotlinx.coroutines.delay
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Duration

data class Author(
    val id: Int = 0,
    val firstName: String,
    val lastName: String
)

@Repository
class AuthorRepository {
  private val authors = listOf(
      Author(1, "Joanne", "Rowling"),
      Author(2, "Herman", "Melville"),
      Author(3, "Anne", "Rice")
  )

  fun findById(id: Int): Author {
    Thread.sleep(100)

    return authors.first { it.id == id }
  }

  fun reactorFindById(id: Int): Mono<Author> {
    return Mono
        .fromCallable { authors.first { it.id == id } }
        .delayElement(Duration.ofMillis(100))
  }

  suspend fun coFindById(id: Int): Author {
    delay(100)

    return authors.first { it.id == id }
  }
}
