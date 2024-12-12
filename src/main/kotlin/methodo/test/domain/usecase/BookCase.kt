package methodo.test.domain.usecase

import methodo.test.domain.model.Book
import methodo.test.domain.port.BookPort

class BookCase(private val bookPort: BookPort) {

    fun createBook(author: String, title: String): Book {
        val book = Book(author, title)
        bookPort.addBook(book)
        return book
    }

    fun listBooks(): Array<Book> {
        return bookPort.listBooks().sortedBy { it.title }.toTypedArray()
    }

    fun deleteBook(book: Book) {
        bookPort.deleteBook(book)
    }
}
