package com.github.anddd7.graphql.model

import com.github.anddd7.persistence.AuthorRepository
import com.github.anddd7.persistence.CompanyPO
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern

data class BookWrapper(
    val id: Int = 0,
    val name: String,
    val pageCount: Int,
    private val authorId: Int,
    private val editorId: Int,
    val company: CompanyPO,
    private val publishedAt: LocalDateTime,
    private val authorRepository: AuthorRepository
) {
  suspend fun author() = authorRepository.findById(authorId)
  suspend fun editor() = authorRepository.findById(editorId)
  fun publishedAt(pattern: String?): String =
      ofPattern(pattern ?: "dd, MMM, yyyy").let(publishedAt::format)
}
