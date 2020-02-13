package com.github.anddd7.graphql.model

import com.github.anddd7.persistence.AuthorPO
import com.github.anddd7.persistence.AuthorRepository
import com.github.anddd7.persistence.BookPO
import com.github.anddd7.persistence.BookRepository
import com.github.anddd7.persistence.CompanyPO
import org.springframework.stereotype.Component

@Component
class BookFactory(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository
) {
  suspend fun findById(id: Int) =
      bookRepository.findById(id).toBook()

  suspend fun findAll() =
      bookRepository.findAll().map { it.toBook() }

  private suspend fun findAuthorById(it: Int) = authorRepository.findById(it)

  private fun BookPO.toBook() = Book(
      id,
      name,
      pageCount,
      authorId,
      editorId,
      company.toCompany(),
      publishedAt
  ) { findAuthorById(it).toAuthor() }

  private fun CompanyPO.toCompany() = Company(name, address)
  private fun AuthorPO.toAuthor() = Author(id, firstName, lastName)
}
