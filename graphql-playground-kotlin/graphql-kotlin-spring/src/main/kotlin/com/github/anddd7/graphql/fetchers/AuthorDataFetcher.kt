package com.github.anddd7.graphql.fetchers

import com.github.anddd7.config.CoroutineDataFetcher
import com.github.anddd7.entity.Author
import com.github.anddd7.entity.AuthorRepository
import com.github.anddd7.entity.Book
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component

@Component("AuthorDataFetcher")
class AuthorDataFetcher(
    private val authorRepository: AuthorRepository
) : CoroutineDataFetcher<Author> {
  override fun type() = Author::class

  override suspend fun fetch(environment: DataFetchingEnvironment): Author =
      authorRepository.coFindById(environment.getSource<Book>().authorId)
}
