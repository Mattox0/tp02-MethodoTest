package methodo.test.domain.port

import methodo.test.domain.model.Book

interface BookPort {
    fun addBook(book: Book)
    fun listBooks(): List<Book>
    fun deleteBook(book: Book)
}