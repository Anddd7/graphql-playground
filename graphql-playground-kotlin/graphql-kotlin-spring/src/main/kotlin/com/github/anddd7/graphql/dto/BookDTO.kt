package com.github.anddd7.graphql.dto

import com.github.anddd7.entity.Book
import com.github.anddd7.entity.Company

data class BookDTO(
    val id: Int = 0,
    val name: String,
    val pageCount: Int,
    val company: CompanyDTO
)

data class CompanyDTO(
    val name: String,
    val address: String
)

fun Book.toDTO() = BookDTO(id, name, pageCount, company.toDTO())
fun Company.toDTO() = CompanyDTO(name, address)
