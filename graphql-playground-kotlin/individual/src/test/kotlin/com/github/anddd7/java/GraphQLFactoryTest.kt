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
  private val graphQLWithErrors = buildWithErrors()

  private fun build() = build(
      listOf(
          object : DataFetcherWrapper<Book> {
            override fun getType() = "Query"
            override fun getFieldName() = "bookById"
            override fun get(environment: graphql.schema.DataFetchingEnvironment) =
                BookRepository.findById(environment.getArgument<kotlin.String>("id").toInt())
          },
          object : DataFetcherWrapper<Author> {
            override fun getType() = "Book"
            override fun getFieldName() = "author"
            override fun get(environment: DataFetchingEnvironment) =
                AuthorRepository.findById(environment.getSource<Book>().authorId)
          }
      )
  )

  private fun buildWithErrors() = build(
      listOf(
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
                throw RuntimeException("Got exception while fetching data")
          }
      )
  )

  private fun build(fetchers: List<DataFetcherWrapper<*>>): GraphQL {
    val uri = ClassLoader.getSystemClassLoader().getResourceAsStream("schema.graphqls")!!
    val schema = String(uri.readAllBytes())

    return graphQLFactory.build(schema, fetchers)
  }

  @Test
  fun `should get data from data fetcher`() {
    val query = "{bookById(id: 1) {id,name,title,pageCount}}"

    val data = graphQL.execute(query).getData<Map<String, Any>>()

    val bookById = data["bookById"] as? Map<String, Any> ?: emptyMap()

    assertThat(bookById["name"]).isEqualTo("Harry Potter and the Philosopher's Stone")
    assertThat(bookById["title"]).isEqualTo("Harry Potter and the Philosopher's Stone")
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

  @Test
  fun `should get formatted data from property data fetcher`() {
    val query = "{bookById(id: 1) {publishedAt(dateFormat: \"dd, MMM, yyyy\")}}"

    val data = graphQL.execute(query).getData<Map<String, Any>>()

    val bookById = data["bookById"] as? Map<String, Any> ?: emptyMap()

    assertThat(bookById["publishedAt"]).isEqualTo("01, Jan, 2020")
  }

  @Test
  fun `should return partial error when get exception while fetching`() {
    val query = "{bookById(id: 1) {title,author{firstName,lastName}}}"

    val result = graphQLWithErrors.execute(query)
    val data = result.getData<Map<String, Any>>()
    val errors = result.errors

    val bookById = data["bookById"] as? Map<String, Any> ?: emptyMap()

    assertThat(bookById["title"]).isEqualTo("Harry Potter and the Philosopher's Stone")
    assertThat(errors).allMatch {
      it.message.contains("Got exception while fetching data")
    }
  }
}
