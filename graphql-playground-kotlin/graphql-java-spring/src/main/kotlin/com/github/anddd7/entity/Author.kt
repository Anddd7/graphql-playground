package com.github.anddd7.entity

data class Author(
    val id: Int = 0,
    val firstName: String,
    val lastName: String
)

object AuthorRepository {
  private val authors = listOf(
      Author(1, "Joanne", "Rowling"),
      Author(2, "Herman", "Melville"),
      Author(3, "Anne", "Rice")
  )

  fun findById(id: Int) = authors.first { it.id == id }
}
