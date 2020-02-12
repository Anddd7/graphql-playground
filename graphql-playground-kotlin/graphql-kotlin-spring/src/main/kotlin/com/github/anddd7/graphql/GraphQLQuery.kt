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
class GraphQLQuery(
    private val bookRepository: BookRepository
) : Query {
  fun bookById(id: Int) = bookRepository.findById(id).toDTO()
  fun books() = bookRepository.findAll().map(Book::toDTO)
}

@Component("AuthorDataFetcher")
class AuthorDataFetcher(
    private val authorRepository: AuthorRepository
) : DataFetcher<AuthorDTO> {
  override fun get(environment: DataFetchingEnvironment): AuthorDTO {
    return authorRepository.findById(environment.getSource<BookDTO>().authorId).toDTO()
  }
}
