package com.github.anddd7.graphql

import com.expediagroup.graphql.spring.operations.Query
import com.github.anddd7.graphql.model.BookDataFetcher
import com.github.anddd7.persistence.BookPO
import com.github.anddd7.persistence.BookRepository
import org.springframework.stereotype.Component

@Component
class BookQuery(
    private val bookRepository: BookRepository,
    private val authorQuery: AuthorQuery
) : Query {
  suspend fun bookById(id: Int) =
      bookRepository.findById(id).toDataFetcher()

  suspend fun books() =
      bookRepository.findAll().map { it.toDataFetcher() }

  private fun BookPO.toDataFetcher() =
      BookDataFetcher(id, name, pageCount, company, authorId, editorId, publishedAt, authorQuery)
}
