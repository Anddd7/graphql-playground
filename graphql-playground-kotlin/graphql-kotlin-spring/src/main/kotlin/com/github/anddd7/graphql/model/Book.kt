package com.github.anddd7.graphql.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern

data class Book(
    val id: Int = 0,
    val name: String,
    val pageCount: Int,
    private val authorId: Int,
    private val editorId: Int,
    val company: Company,
    private val publishedAt: LocalDateTime,
    private val authorById: suspend (Int) -> Author
) {
  suspend fun author() = authorById(authorId)
  suspend fun editor() = authorById(editorId)
  fun publishedAt(pattern: String?): String =
      ofPattern(pattern ?: "dd, MMM, yyyy").let(publishedAt::format)
}

data class Company(
    val name: String,
    val address: String
)

data class Author(
    val id: Int = 0,
    val firstName: String,
    val lastName: String
)
