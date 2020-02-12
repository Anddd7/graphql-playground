package com.github.anddd7.graphql

import com.expediagroup.graphql.spring.operations.Query
import com.github.anddd7.entity.BookRepository
import com.github.anddd7.graphql.dto.toDTO
import org.springframework.stereotype.Component

@Component
class BookQuery(
    private val bookRepository: BookRepository
) : Query {
  fun bookById(id: Int) = bookRepository.findById(id).toDTO()
}

