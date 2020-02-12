package com.github.anddd7.graphql.dto

import com.github.anddd7.entity.AuthorRepository
import com.github.anddd7.entity.Book
import com.github.anddd7.entity.Company

data class BookDTO(
    val id: Int = 0,
    val name: String,
    val pageCount: Int,
    val company: CompanyDTO,
    private val authorId: Int,
    private val authorRepository: AuthorRepository
) {
  fun author() = authorRepository.findById(authorId).toDTO()
}

data class CompanyDTO(
    val name: String,
    val address: String
)

fun Book.toDTO(authorRepository: AuthorRepository) = BookDTO(
    id,
    name,
    pageCount,
    company.toDTO(),
    authorId,
    authorRepository
)

fun Company.toDTO() = CompanyDTO(name, address)
