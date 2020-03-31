package com.github.anddd7.graphql.plugins

import com.expediagroup.graphql.spring.exception.SimpleKotlinGraphQLError
import com.expediagroup.graphql.spring.execution.DataLoaderRegistryFactory
import com.expediagroup.graphql.spring.execution.SimpleQueryHandler
import com.expediagroup.graphql.spring.model.GraphQLRequest
import com.expediagroup.graphql.spring.model.GraphQLResponse
import com.expediagroup.graphql.spring.model.toExecutionInput
import com.expediagroup.graphql.spring.model.toGraphQLResponse
import graphql.GraphQL
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactor.ReactorContext
import org.springframework.stereotype.Component
import reactor.util.context.Context

@ExperimentalCoroutinesApi
@Component
class ContextualQueryHandler(
    private val graphql: GraphQL,
    private val dataLoaderRegistryFactory: DataLoaderRegistryFactory
) : SimpleQueryHandler(graphql, dataLoaderRegistryFactory) {

  @Suppress("TooGenericExceptionCaught")
  override suspend fun executeQuery(request: GraphQLRequest): GraphQLResponse {
    val context = kotlin.coroutines.coroutineContext[ReactorContext]?.context?.let(Context::toQueryContext)
    val input = request.toExecutionInput(context, dataLoaderRegistryFactory.generate())

    return try {
      graphql.executeAsync(input).await().toGraphQLResponse()
    } catch (e: Exception) {
      GraphQLResponse(errors = listOf(SimpleKotlinGraphQLError(e)))
    }
  }
}

data class QueryContext(
    val field: String,
    val data: Map<String, Any>
)

fun Context.toQueryContext() = QueryContext(
    field = get("key of field in reactor context"),
    data = get("key of data in reactor context")
)

fun QueryContext.toReactorContext() = Context.of(
    "key of field in reactor context", field,
    "key of data in reactor context", data
)
