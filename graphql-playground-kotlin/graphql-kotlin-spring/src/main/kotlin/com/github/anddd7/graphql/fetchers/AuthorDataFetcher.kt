package com.github.anddd7.graphql.fetchers

import com.github.anddd7.entity.Author
import com.github.anddd7.entity.AuthorRepository
import com.github.anddd7.entity.Book
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component

@Component("AuthorDataFetcher")
class AuthorDataFetcher(
    private val authorRepository: AuthorRepository
) : DataFetcher<Author> {
  override fun get(environment: DataFetchingEnvironment): Author {
    return authorRepository.findById(environment.getSource<Book>().authorId)
  }
}
