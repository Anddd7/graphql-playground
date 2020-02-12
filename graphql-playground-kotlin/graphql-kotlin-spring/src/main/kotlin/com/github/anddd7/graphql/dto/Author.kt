package com.github.anddd7.graphql.dto

import com.github.anddd7.entity.Author

data class AuthorDTO(
    val id: Int = 0,
    val firstName: String,
    val lastName: String
)

fun Author.toDTO() = AuthorDTO(id, firstName, lastName)
