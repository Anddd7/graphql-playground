package com.github.anddd7.entity

data class Message(
    val id: Int = 0,
    val content: String,
    val author: String
)

data class MessageInput(
    val content: String,
    val author: String
) {
  fun toMessage(id: Int) = Message(id, content, author)

  companion object {
    fun fromArgument(argument: Map<String, String>): MessageInput {
      return MessageInput(content = argument.getValue("content"), author = argument.getValue("author"))
    }
  }
}
