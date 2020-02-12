package com.github.anddd7

import com.expediagroup.graphql.SchemaGeneratorConfig
import com.expediagroup.graphql.TopLevelObject
import com.expediagroup.graphql.toSchema
import com.github.anddd7.entity.BookRepository
import com.github.anddd7.graphql.GraphQLQuery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class GraphQLSchemaTest {

  @MockK
  private lateinit var bookRepository: BookRepository

  @Test
  fun `should generate schema by kotlin classes`() {
    val bookService = GraphQLQuery(bookRepository)

    val config = SchemaGeneratorConfig(supportedPackages = listOf("com.github.anddd7.query"))
    val queries = listOf(TopLevelObject(bookService))
    val mutations = emptyList<TopLevelObject>()

    val schema = toSchema(config, queries, mutations)

    assertThat(schema).isNotNull
  }
}
