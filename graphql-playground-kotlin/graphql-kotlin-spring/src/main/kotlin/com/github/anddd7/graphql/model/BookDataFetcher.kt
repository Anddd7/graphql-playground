package com.github.anddd7.graphql.model

import com.github.anddd7.graphql.AuthorQuery
import com.github.anddd7.persistence.CompanyPO
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern

data class BookDataFetcher(
    val id: Int = 0,
    val name: String,
    val pageCount: Int,
    val company: CompanyPO,
    // internal fields
    private val authorId: Int,
    private val editorId: Int,
    private val publishedAt: LocalDateTime,
    private val authorQuery: AuthorQuery
) {
  suspend fun author() = authorQuery.authorById(authorId)
  suspend fun editor() = authorQuery.authorById(editorId)
  fun publishedAt(pattern: String?): String =
      ofPattern(pattern ?: "dd, MMM, yyyy").let(publishedAt::format)
}
