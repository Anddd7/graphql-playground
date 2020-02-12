package com.github.anddd7

import com.github.anddd7.graphql.DataFetcherWrapper
import com.github.anddd7.factory.GraphQLFactory
import graphql.GraphQL

fun build(fetchers: List<DataFetcherWrapper<*>>): GraphQL {
  val uri = ClassLoader.getSystemClassLoader().getResourceAsStream("schema.graphqls")!!
  val schema = String(uri.readAllBytes())

  return GraphQLFactory.buildGraphQL(schema, fetchers)
}
