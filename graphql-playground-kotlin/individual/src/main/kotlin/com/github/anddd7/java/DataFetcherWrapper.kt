package com.github.anddd7.java

import graphql.schema.DataFetcher

interface DataFetcherWrapper<T> : DataFetcher<T> {
  fun getType(): String
  fun getFieldName(): String
}
