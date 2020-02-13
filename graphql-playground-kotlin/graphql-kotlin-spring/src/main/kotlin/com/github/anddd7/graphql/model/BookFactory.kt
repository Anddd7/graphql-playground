package com.github.anddd7.graphql.model

import com.github.anddd7.persistence.AuthorRepository
import com.github.anddd7.persistence.BookPO
import com.github.anddd7.persistence.BookRepository
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

  private fun BookPO.toBook() = BookWrapper(
      id,
      name,
      pageCount,
      authorId,
      editorId,
      company,
      publishedAt
  ) { authorRepository.findById(it) }
}