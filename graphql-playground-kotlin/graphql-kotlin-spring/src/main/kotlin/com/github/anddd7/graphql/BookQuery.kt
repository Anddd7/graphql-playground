package com.github.anddd7.graphql

import com.expediagroup.graphql.spring.operations.Query
import com.github.anddd7.entity.BookRepository
import org.springframework.stereotype.Component

@Component
class BookQuery(
    private val bookRepository: BookRepository
) : Query {
  suspend fun bookById(id: Int) = bookRepository.coFindById(id)
  suspend fun books() = bookRepository.coFindAll()
}

