package com.jicay.bookmanagement.domain.model

data class Book(val name: String, val author: String, val reserved: Boolean = false)
