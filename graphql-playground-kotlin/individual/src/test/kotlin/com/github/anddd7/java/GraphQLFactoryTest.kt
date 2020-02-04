package com.github.anddd7.java

import graphql.schema.StaticDataFetcher
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GraphQLFactoryTest {

  @MockK
  private val graphQLFactory: GraphQLFactory = GraphQLFactory()

  @Test
  fun `should build graphql with schema and data fetcher`() {
    val schema = "type Query{hello: String}"
    val type = "Query"
    val field = "hello"
    val dataFetcher = StaticDataFetcher("world")
    val query = "{hello}"

    val graphQL = graphQLFactory.build(schema, type, field, dataFetcher)
    val result = graphQL.execute(query)
    val world = result.getData<Map<String, Any>>()[field]

    assertThat(world).isEqualTo("world")
  }
}
