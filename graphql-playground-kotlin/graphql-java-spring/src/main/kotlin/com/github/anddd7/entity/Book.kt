package com.github.anddd7.entity

import graphql.schema.DataFetchingEnvironment
import java.time.LocalDateTime
import java.time.LocalDateTime.of
import java.time.format.DateTimeFormatter.ofPattern

data class Book(
    val id: Int = 0,
    val name: String,
    val pageCount: Int,
    val authorId: Int,
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

object BookRepository {
  private val github = Company("Github", "https://github.com")
  private val thoughtworks = Company("ThoughtWorks", "https://www.thoughtworks.com")

  private val books = listOf(
      Book(
          1, "Harry Potter and the Philosopher's Stone",
          223, 1, github, of(2020, 1, 1, 0, 0, 0)
      ),
      Book(2, "Moby Dick",
          635, 2, github, of(2020, 1, 1, 0, 0, 0)
      ),
      Book(3, "Interview with the vampire's Stone",
          371, 3, thoughtworks, of(2020, 1, 1, 0, 0, 0)
      )
  )

  fun findById(id: Int) = books.first { it.id == id }
}
