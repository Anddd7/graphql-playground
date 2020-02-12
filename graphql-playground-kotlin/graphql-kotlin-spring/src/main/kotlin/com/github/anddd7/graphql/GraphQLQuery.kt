package com.github.anddd7.graphql

import com.expediagroup.graphql.spring.operations.Query
import com.github.anddd7.entity.AuthorRepository
import com.github.anddd7.entity.BookRepository
import com.github.anddd7.graphql.dto.toDTO
import org.springframework.stereotype.Component

@Component
class GraphQLQuery(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository
) : Query {
  fun bookById(id: Int) = bookRepository.findById(id).toDTO(authorRepository)
  fun books() = bookRepository.findAll().map { it.toDTO(authorRepository) }
}
