package com.github.anddd7.java

import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GraphQLFactoryTest {

  @MockK
  private val graphQLFactory: GraphQLFactory = GraphQLFactory()

  @Test
  fun `should return static data`() {
    val result = graphQLFactory.execute()

    assertThat(result).containsEntry("hello", "world")
  }
}
