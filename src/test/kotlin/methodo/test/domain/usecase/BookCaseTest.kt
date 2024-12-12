package methodo.test.domain.usecase

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSortedBy
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import methodo.test.domain.model.Book
import methodo.test.domain.port.BookPort

class BookCaseTest : FunSpec({

    val bookPort = mockk<BookPort>(relaxed = true)
    val bookCase = BookCase(bookPort)

    test("createBook should add a book to BookPort") {
        checkAll(Arb.string(), Arb.string()) { author, title ->
            val book = Book(author, title)
            bookCase.createBook(author, title)
            verify { bookPort.addBook(book) }
        }
    }

    test("createBook should create a Book with the correct properties") {
        checkAll(Arb.string(), Arb.string()) { author, title ->
            val book = bookCase.createBook(author, title)

            book.author shouldBe author
            book.title shouldBe title
        }
    }

    test("listBooks should return sorted books from BookPort") {
        checkAll(Arb.string(), Arb.string(), Arb.string(), Arb.string()) { author1, title1, author2, title2 ->
            val books = listOf(
                Book(author1, title1),
                Book(author2, title2)
            )
            every { bookPort.listBooks() } returns books

            val sortedBooks = bookCase.listBooks()
            val expectedSortedBooks = books.sortedBy { it.title }

            sortedBooks shouldBe expectedSortedBooks
        }
    }

    test("deleteBook should remove a book from BookPort") {
        checkAll(Arb.string(), Arb.string()) { author, title ->
            val book = Book(author, title)
            bookCase.deleteBook(book)
            verify { bookPort.deleteBook(book) }
        }
    }

    test("listBooks should return books sorted by title") {
        checkAll(Arb.string(), Arb.string(), Arb.string(), Arb.string()) { author1, title1, author2, title2 ->
            val books = listOf(
                Book(author1, title1),
                Book(author2, title2)
            )
            every { bookPort.listBooks() } returns books

            val sortedBooks = bookCase.listBooks()

            sortedBooks shouldBeSortedBy { it.title }
        }
    }

    test("listBooks should return a non-empty list if books are added") {
        checkAll(Arb.string(), Arb.string(), Arb.string(), Arb.string()) { author1, title1, author2, title2 ->
            every { bookPort.listBooks() } returns listOf()

            val book1 = Book(author1, title1)
            val book2 = Book(author2, title2)

            every { bookPort.addBook(book1) } returns Unit
            every { bookPort.addBook(book2) } returns Unit
            every { bookPort.listBooks() } returns listOf(book1, book2)

            bookCase.createBook(author1, title1)
            bookCase.createBook(author2, title2)

            val books = bookCase.listBooks()

            books.size shouldBe 2
            books shouldContain book1
            books shouldContain book2
        }
    }
})