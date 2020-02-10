package com.github.anddd7.java

import com.github.anddd7.java.entity.Message
import com.github.anddd7.java.entity.MessageInput
import graphql.schema.DataFetchingEnvironment
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

typealias DataBase = MutableMap<Int, Message>

internal class GraphQLMutationTest {
  private val log = LoggerFactory.getLogger(this.javaClass)

  private val graphQL = build()
  private val mockDB: DataBase = mutableMapOf()

  fun DataBase.findById(id: Int): Message? {
    log.info("trying to find message by id: $id")
    return mockDB[id].apply {
      log.info("find $this")
    }
  }

  fun DataBase.save(message: Message): Message {
    log.info("trying to save $message")
    return message.apply {
      mockDB[id] = this
      log.info("saved successfully")
    }
  }

  @AfterEach
  internal fun tearDown() {
    mockDB.clear()
  }

  private fun build() = build(
      listOf(
          object : DataFetcherWrapper<Message> {
            override fun getType() = "Mutation"
            override fun getFieldName() = "createMessage"
            override fun get(environment: DataFetchingEnvironment): Message {
              val input = MessageInput.fromArgument(environment.getArgument<Map<String, String>>("input"))
              val message = input.toMessage(mockDB.size + 1)

              return mockDB.save(message)
            }
          },
          object : DataFetcherWrapper<Message> {
            override fun getType() = "Mutation"
            override fun getFieldName() = "updateMessage"
            override fun get(environment: DataFetchingEnvironment): Message {
              val id = environment.getArgument<String>("id").toInt()
              val input = MessageInput.fromArgument(environment.getArgument<Map<String, String>>("input"))
              val updatedMessage = mockDB.findById(id)
                  ?.copy(id = id, content = input.content, author = input.author)
                  ?: throw IllegalArgumentException("No such id")

              return mockDB.save(updatedMessage)
            }
          }
      )
  )

  @Test
  fun `should save a new message and return specific fields with mutation`() {
    val mutation = "mutation{createMessage(input: {content:\"Please clear your table after finished your work\", author:\"admin\"}) {content,author}}"

    val data = graphQL.execute(mutation).getData<Map<String, Any>>()

    val response = data["createMessage"] as? Map<String, Any> ?: emptyMap()

    assertThat(response["content"]).isEqualTo("Please clear your table after finished your work")
    assertThat(response["author"]).isEqualTo("admin")
  }

  @Test
  fun `should update the new message and return specific fields with mutation`() {
    val create = "mutation{createMessage(input:{content:\"Please clear your table after finished your work\", author:\"admin\"}){id}}"
    val createResponse = graphQL.execute(create)
        .getData<Map<String, Any>>()["createMessage"] as? Map<String, String>
        ?: emptyMap()
    val id = createResponse.getValue("id")

    val update = "mutation{updateMessage(id:$id,input:{content:\"Already done\", author:\"user\"}){content,author}}"
    val updateResponse = graphQL.execute(update)
        .getData<Map<String, Any>>()["updateMessage"] as? Map<String, String>
        ?: emptyMap()

    assertThat(updateResponse["content"]).isEqualTo("Already done")
    assertThat(updateResponse["author"]).isEqualTo("user")
  }
}
