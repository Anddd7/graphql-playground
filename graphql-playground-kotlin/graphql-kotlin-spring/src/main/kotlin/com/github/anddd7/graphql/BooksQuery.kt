package com.github.anddd7.graphql

import com.expediagroup.graphql.spring.operations.Query
import com.github.anddd7.entity.AuthorRepository
import com.github.anddd7.entity.Book
import com.github.anddd7.entity.BookRepository
import com.github.anddd7.graphql.dto.AuthorDTO
import com.github.anddd7.graphql.dto.BookDTO
import com.github.anddd7.graphql.dto.toDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component

@Component
class BooksQuery(
    private val bookRepository: BookRepository
) : Query {
  fun books() = bookRepository.findAll().map(Book::toDTO)
}
