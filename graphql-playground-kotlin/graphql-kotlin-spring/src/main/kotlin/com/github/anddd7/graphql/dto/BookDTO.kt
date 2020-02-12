package com.github.anddd7.graphql.dto

import com.expediagroup.graphql.annotations.GraphQLIgnore
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.anddd7.entity.Book
import com.github.anddd7.entity.Company
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern

data class BookDTO(
    val id: Int = 0,
    val name: String,
    val pageCount: Int,
    val company: CompanyDTO,
    val authorId: Int,
    val editorId: Int,
    @GraphQLIgnore
    val publishedAt: LocalDateTime
) {
  @JsonIgnore
  lateinit var author: AuthorDTO
  @JsonIgnore
  lateinit var editor: AuthorDTO

  fun publishedAt(pattern: String?) =
      ofPattern(pattern ?: "dd, MMM, yyyy").let(publishedAt::format)
}

data class CompanyDTO(
    val name: String,
    val address: String
)

fun Book.toDTO() = BookDTO(
    id,
    name,
    pageCount,
    company.toDTO(),
    authorId,
    editorId,
    publishedAt
)

fun Company.toDTO() = CompanyDTO(name, address)
