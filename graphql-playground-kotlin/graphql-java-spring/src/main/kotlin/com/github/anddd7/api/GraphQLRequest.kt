package com.github.anddd7.api

data class GraphQLRequest(
    val query: String? = null,
    val variables: Map<String, Any> = emptyMap()
)
