package com.jicay.bookmanagement.domain.usecase

import com.jicay.bookmanagement.domain.model.Book
import com.jicay.bookmanagement.domain.port.BookPort
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class BookUseCase(private val bookPort: BookPort) {
    fun getAllBooks(): List<Book> {
        return bookPort.getAllBooks().sortedBy {
            it.name.lowercase()
        }
    }

    fun getBook(bookId: Int): Book? {
        return bookPort.getBook(bookId)
    }

    fun addBook(book: Book) {
        bookPort.createBook(book)
    }

    fun reserveBook(bookId: Int) {
        if (isReservedBook(bookId)) {
            throw IllegalStateException("Book is already reserved")
        }
        bookPort.reserveBook(bookId)
    }

    private fun isReservedBook(bookId: Int): Boolean {
        val book = bookPort.getBook(bookId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found")
        return book.reserved
    }
}