package com.github.anddd7.persistence

import kotlinx.coroutines.delay
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.LocalDateTime.of

data class BookPO(
    val id: Int = 0,
    val name: String,
    val pageCount: Int,
    val authorId: Int,
    val editorId: Int,
    val company: CompanyPO,
    val publishedAt: LocalDateTime
)

data class CompanyPO(
    val name: String,
    val address: String
)

@Repository
class BookRepository {
  private val github = CompanyPO("Github", "https://github.com")
  private val thoughtworks = CompanyPO("ThoughtWorks", "https://www.thoughtworks.com")

  private val books = listOf(
      BookPO(
          1, "Harry Potter and the Philosopher's Stone",
          223, 1, 1, github, of(2020, 1, 1, 0, 0, 0)
      ),
      BookPO(2, "Moby Dick",
          635, 2, 2, github, of(2020, 1, 1, 0, 0, 0)
      ),
      BookPO(3, "Interview with the vampire's Stone",
          371, 3, 3, thoughtworks, of(2020, 1, 1, 0, 0, 0)
      )
  )

  suspend fun findById(id: Int): BookPO {
    delay(100)
    return books.first { it.id == id }
  }

  suspend fun findAll(): List<BookPO> {
    delay(100)
    return books
  }
}
