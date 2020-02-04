package com.github.anddd7.java.entity

data class Book(
    val id: Int = 0,
    val name: String,
    val pageCount: Int,
    val authorId: Int,
    val company: Company
)

data class Company(
    val name: String,
    val address: String
)

object BookRepository {
  private val github = Company("Github", "https://github.com")
  private val thoughtworks = Company("ThoughtWorks", "https://www.thoughtworks.com")

  private val books = listOf(
      Book(1, "Harry Potter and the Philosopher's Stone", 223, 1, github),
      Book(2, "Moby Dick", 635, 2, github),
      Book(3, "Interview with the vampire's Stone", 371, 3, thoughtworks)
  )

  fun findById(id: Int) = books.first { it.id == id }
}
