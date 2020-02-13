package com.github.anddd7.persistence

import kotlinx.coroutines.delay
import org.springframework.stereotype.Repository

data class AuthorPO(
    val id: Int = 0,
    val firstName: String,
    val lastName: String
)

@Repository
class AuthorRepository {
  private val authors = listOf(
      AuthorPO(1, "Joanne", "Rowling"),
      AuthorPO(2, "Herman", "Melville"),
      AuthorPO(3, "Anne", "Rice")
  )

  suspend fun findById(id: Int): AuthorPO {
    delay(100)
    return authors.first { it.id == id }
  }
}
