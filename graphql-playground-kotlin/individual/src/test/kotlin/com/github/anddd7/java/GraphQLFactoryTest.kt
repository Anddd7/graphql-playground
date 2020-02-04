package com.github.anddd7.java

import com.github.anddd7.java.entity.Author
import com.github.anddd7.java.entity.AuthorRepository
import com.github.anddd7.java.entity.Book
import com.github.anddd7.java.entity.BookRepository
import graphql.GraphQL
import graphql.schema.DataFetchingEnvironment
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GraphQLFactoryTest {

  @MockK
  private val graphQLFactory: GraphQLFactory = GraphQLFactory()
  private val graphQL = build()

  private fun build(): GraphQL {
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
    return graphQLFactory.build(schema, fetchers)
  }

  @Test
  fun `should get data from data fetcher`() {
    val query = "{bookById(id: 1) {id,name,pageCount}}"

    val data = graphQL.execute(query).getData<Map<String, Any>>()

    val bookById = data["bookById"] as? Map<String, Any> ?: emptyMap()

    assertThat(bookById["name"]).isEqualTo("Harry Potter and the Philosopher's Stone")
    assertThat(bookById["pageCount"]).isEqualTo(223)
  }

  @Test
  fun `should get nested data from data fetcher`() {
    val query = "{bookById(id: 1) {author{firstName,lastName}}}"

    val data = graphQL.execute(query).getData<Map<String, Any>>()

    val bookById = data["bookById"] as? Map<String, Any> ?: emptyMap()
    val author = bookById["author"] as? Map<String, Any> ?: emptyMap()

    assertThat(author["firstName"]).isEqualTo("Joanne")
    assertThat(author["lastName"]).isEqualTo("Rowling")
  }

  @Test
  fun `should get nested data from field object`() {
    val query = "{bookById(id: 1) {company{name,address}}}"

    val data = graphQL.execute(query).getData<Map<String, Any>>()

    val bookById = data["bookById"] as? Map<String, Any> ?: emptyMap()
    val company = bookById["company"] as? Map<String, Any> ?: emptyMap()

    assertThat(company["name"]).isEqualTo("Github")
    assertThat(company["address"]).isEqualTo("https://github.com")
  }
}
