package com.github.anddd7.java

import com.github.anddd7.java.entity.Author
import com.github.anddd7.java.entity.AuthorRepository
import com.github.anddd7.java.entity.Book
import com.github.anddd7.java.entity.BookRepository
import graphql.schema.DataFetchingEnvironment
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GraphQLFactoryTest {

  @MockK
  private val graphQLFactory: GraphQLFactory = GraphQLFactory()

  @Test
  fun `should build graphql with schema and data fetcher`() {
    val uri = ClassLoader.getSystemClassLoader().getResourceAsStream("schema.graphqls")!!
    val schema = String(uri.readAllBytes())

    val fetchers: List<DataFetcherWrapper<*>> = listOf(
        object : DataFetcherWrapper<Book> {
          override fun getType() = "Query"
          override fun getFieldName() = "bookById"
          override fun get(environment: DataFetchingEnvironment) =
              BookRepository.findById(environment.getArgument<String>("id").toInt())
        },
        object : DataFetcherWrapper<Author> {
          override fun getType() = "Book"
          override fun getFieldName() = "author"
          override fun get(environment: DataFetchingEnvironment) =
              AuthorRepository.findById(environment.getSource<Book>().authorId)
        }
    )
    val query = "{bookById(id: 1) {id,name,pageCount,author{firstName,lastName}}}"

    val graphQL = graphQLFactory.build(schema, fetchers)
    val data = graphQL.execute(query).getData<Map<String, Any>>()

    val bookById = data["bookById"] as? Map<String, Any> ?: emptyMap()

    assertThat(bookById["name"]).isEqualTo("Harry Potter and the Philosopher's Stone")
  }
}
