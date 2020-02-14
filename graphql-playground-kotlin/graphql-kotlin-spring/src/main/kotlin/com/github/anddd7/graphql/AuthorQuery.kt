package com.github.anddd7.graphql

import com.expediagroup.graphql.spring.operations.Query
import com.github.anddd7.persistence.AuthorRepository
import org.springframework.stereotype.Component

@Component
class AuthorQuery(
    private val authorRepository: AuthorRepository
) : Query {
  suspend fun authorById(id: Int) = authorRepository.findById(id)
}
