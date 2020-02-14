package com.github.anddd7

import com.github.anddd7.factory.GraphQLFactory
import com.github.anddd7.graphql.KotlinGraphQLFactory
import com.github.anddd7.factory.DataFetcherWrapper
import com.github.anddd7.graphql.KotlinFetcher
import graphql.GraphQL

fun build(fetchers: List<DataFetcherWrapper<*>>): GraphQL {
  val uri = ClassLoader.getSystemClassLoader().getResourceAsStream("schema.graphqls")!!
  val schema = String(uri.readAllBytes())

  return GraphQLFactory.buildGraphQL(schema, fetchers)
}

fun buildKotlin(fetchers: List<KotlinFetcher>): GraphQL {
  val uri = ClassLoader.getSystemClassLoader().getResourceAsStream("schema.graphqls")!!
  val schema = String(uri.readAllBytes())

  return KotlinGraphQLFactory.buildGraphQL(schema, fetchers)
}
