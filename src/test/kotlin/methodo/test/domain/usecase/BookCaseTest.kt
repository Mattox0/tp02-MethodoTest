package methodo.test.domain.usecase

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSortedBy
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import methodo.test.domain.model.Book
import methodo.test.domain.port.BookPort

class BookCaseTest : FunSpec({

    val bookPort = mockk<BookPort>(relaxed = true)
    val bookCase = BookCase(bookPort)

    test("createBook should add a book to BookPort") {
        val author = "Author A"
        val title = "Title A"
        val book = Book(author, title)
        bookCase.createBook(author, title)
        verify { bookPort.addBook(book) }
    }

    test("createBook should create a Book with the correct properties") {
        val author = "Author A"
        val title = "Title A"
        val book = bookCase.createBook(author, title)

        book.author shouldBe author
        book.title shouldBe title
    }

    test("listBooks should return sorted books from BookPort") {
        val books = listOf(
            Book("Author B", "Title C"),
            Book("Author A", "Title A")
        )
        every { bookPort.listBooks() } returns books
        val sortedBooks = bookCase.listBooks()
        sortedBooks shouldBe arrayOf(
            Book("Author A", "Title A"),
            Book("Author B", "Title B")
        )
    }

    test("deleteBook should remove a book from BookPort") {
        val book = Book("Author A", "Title A")
        bookCase.deleteBook(book)
        verify { bookPort.deleteBook(book) }
    }

    test("listBooks should return books sorted by title") {
        val books = listOf(
            Book("Author B", "Title Z"),
            Book("Author A", "Title A"),
            Book("Author C", "Title M")
        )
        every { bookPort.listBooks() } returns books
        val sortedBooks = bookCase.listBooks()
        sortedBooks shouldBeSortedBy  { it.title }
    }

    test("listBooks should return a non-empty list if books are added") {
        val book1 = Book("Author A", "Title A")
        val book2 = Book("Author B", "Title B")

        every { bookPort.listBooks() } returns listOf(book1, book2)

        bookCase.createBook("Author A", "Title A")
        bookCase.createBook("Author B", "Title B")

        val books = bookCase.listBooks()

        books.size shouldBe 2
        books shouldContain book1
        books shouldContain book2
    }
})
