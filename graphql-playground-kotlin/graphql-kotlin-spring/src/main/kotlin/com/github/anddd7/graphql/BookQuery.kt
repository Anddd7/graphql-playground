package com.github.anddd7.graphql

import com.expediagroup.graphql.spring.operations.Query
import com.github.anddd7.graphql.model.BookFactory
import org.springframework.stereotype.Component

@Component
class BookQuery(
    private val bookFactory: BookFactory
) : Query {
  suspend fun bookById(id: Int) = bookFactory.findById(id)
  suspend fun books() = bookFactory.findAll()
}

