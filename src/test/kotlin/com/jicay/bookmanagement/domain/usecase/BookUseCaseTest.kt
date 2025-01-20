package com.jicay.bookmanagement.domain.usecase

import com.jicay.bookmanagement.domain.model.Book
import com.jicay.bookmanagement.domain.port.BookPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.web.server.ResponseStatusException

class BookUseCaseTest : FunSpec({

    val bookPort = mockk<BookPort>()
    val bookUseCase = BookUseCase(bookPort)

    test("get all books should returns all books sorted by name") {
        every { bookPort.getAllBooks() } returns listOf(
            Book("Les Misérables", "Victor Hugo"),
            Book("Hamlet", "William Shakespeare")
        )

        val res = bookUseCase.getAllBooks()

        res.shouldContainExactly(
            Book("Hamlet", "William Shakespeare"),
            Book("Les Misérables", "Victor Hugo")
        )
    }

    test("add book") {
        justRun { bookPort.createBook(any()) }

        val book = Book("Les Misérables", "Victor Hugo")

        bookUseCase.addBook(book)

        verify(exactly = 1) { bookPort.createBook(book) }
    }

    test("get book by id should return book when it exists") {
        val expectedBook = Book("Les Misérables", "Victor Hugo")
        every { bookPort.getBook(1) } returns expectedBook

        val res = bookUseCase.getBook(1)

        res shouldBe expectedBook
    }

    test("get book by id should return null when book doesn't exist") {
        every { bookPort.getBook(1) } returns null

        val res = bookUseCase.getBook(1)

        res shouldBe null
    }

    test("reserve book should succeed when book exists and is not reserved") {
        val book = Book("Les Misérables", "Victor Hugo", reserved = false)
        every { bookPort.getBook(1) } returns book
        justRun { bookPort.reserveBook(1) }

        bookUseCase.reserveBook(1)

        verify(exactly = 1) { bookPort.reserveBook(1) }
    }

    test("reserve book should throw IllegalStateException when book is already reserved") {
        val book = Book("Les Misérables", "Victor Hugo", reserved = true)
        every { bookPort.getBook(1) } returns book

        val exception = shouldThrow<IllegalStateException> {
            bookUseCase.reserveBook(1)
        }
        exception.message shouldBe "Book is already reserved"
    }

    test("reserve book should throw ResponseStatusException when book doesn't exist") {
        every { bookPort.getBook(1) } returns null

        val exception = shouldThrow<ResponseStatusException> {
            bookUseCase.reserveBook(1)
        }
        exception.statusCode.value() shouldBe 404
        exception.reason shouldBe "Book not found"
    }

    test("get book should return reserved status when book is reserved") {
        val book = Book("Les Misérables", "Victor Hugo", reserved = true)
        every { bookPort.getBook(1) } returns book

        val res = bookUseCase.getBook(1)

        res shouldBe book
        res?.reserved shouldBe true
    }
})