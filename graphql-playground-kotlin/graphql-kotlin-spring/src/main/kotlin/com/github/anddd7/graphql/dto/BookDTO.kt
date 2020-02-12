package com.github.anddd7.graphql.dto

import com.expediagroup.graphql.annotations.GraphQLIgnore
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.anddd7.entity.Book
import com.github.anddd7.entity.Company

data class BookDTO(
    val id: Int = 0,
    val name: String,
    val pageCount: Int,
    val company: CompanyDTO,
    @GraphQLIgnore
    val authorId: Int
) {
  @JsonIgnore
  lateinit var author: AuthorDTO
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
    authorId
)

fun Company.toDTO() = CompanyDTO(name, address)
