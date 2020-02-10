package com.github.anddd7.java

import graphql.GraphQL

private val graphQLFactory = GraphQLFactory()

fun build(fetchers: List<DataFetcherWrapper<*>>): GraphQL {
  val uri = ClassLoader.getSystemClassLoader().getResourceAsStream("schema.graphqls")!!
  val schema = String(uri.readAllBytes())

  return graphQLFactory.build(schema, fetchers)
}
