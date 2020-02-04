package com.github.anddd7.java.entity

data class Book(
    val id: Int = 0,
    val name: String,
    val pageCount: Int,
    val authorId: Int
)

object BookRepository {
  private val books = listOf(
      Book(1, "Harry Potter and the Philosopher's Stone", 223, 1),
      Book(2, "Moby Dick", 635, 2),
      Book(3, "Interview with the vampire's Stone", 371, 3)
  )

  fun findById(id: Int) = books.first { it.id == id }
}
