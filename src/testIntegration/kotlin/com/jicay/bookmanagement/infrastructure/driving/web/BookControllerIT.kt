package com.jicay.bookmanagement.infrastructure.driving.web

import com.jicay.bookmanagement.domain.model.Book
import com.jicay.bookmanagement.domain.usecase.BookUseCase
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest
class BookControllerIT(
    @MockkBean private val bookUseCase: BookUseCase,
    private val mockMvc: MockMvc
) : FunSpec({
    extension(SpringExtension)

    test("rest route get books") {
        every { bookUseCase.getAllBooks() } returns listOf(Book("The Hobbit", "J.R.R. Tolkien"))

        mockMvc.get("/books")
            .andExpect {
                status { isOk() }
                content { content { APPLICATION_JSON } }
                content {
                    json(
                        // language=json
                        """
                        [
                          {
                            "name": "The Hobbit",
                            "author": "J.R.R. Tolkien"
                          }
                        ]
                        """.trimIndent()
                    )
                }
            }
    }

    test("rest route post book") {
        justRun { bookUseCase.addBook(any()) }

        mockMvc.post("/books") {
            // language=json
            content = """
                {
                  "name": "Dune",
                  "author": "Frank Herbert"
                }
            """.trimIndent()
            contentType = APPLICATION_JSON
            accept = APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
        }

        val expected = Book(
            name = "Dune",
            author = "Frank Herbert"
        )

        verify(exactly = 1) { bookUseCase.addBook(expected) }
    }

    test("rest route post book should return 400 when body is not good") {
        justRun { bookUseCase.addBook(any()) }

        mockMvc.post("/books") {
            // language=json
            content = """
                {
                  "title": "Foundation",
                  "author": "Isaac Asimov"
                }
            """.trimIndent()
            contentType = APPLICATION_JSON
            accept = APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { bookUseCase.addBook(any()) }
    }

    test("get book by id should return book when found") {
        val book = Book("Neuromancer", "William Gibson")
        every { bookUseCase.getBook(1) } returns book

        mockMvc.get("/books/1")
            .andExpect {
                status { isOk() }
                content { content { APPLICATION_JSON } }
                content {
                    json(
                        """
                        {
                          "name": "Neuromancer",
                          "author": "William Gibson"
                        }
                        """.trimIndent()
                    )
                }
            }
    }

    test("get book by id should return 404 when not found") {
        every { bookUseCase.getBook(1) } returns null

        mockMvc.get("/books/1")
            .andExpect {
                status { isNotFound() }
            }
    }

    test("reserve book should return 200 when successful") {
        justRun { bookUseCase.reserveBook(1) }

        mockMvc.post("/books/reserved/1") {
            contentType = APPLICATION_JSON
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }

        verify(exactly = 1) { bookUseCase.reserveBook(1) }
    }

    test("reserve book should return 404 when book not found") {
        every { bookUseCase.reserveBook(1) } throws IllegalArgumentException("Book not found")

        mockMvc.post("/books/reserved/1") {
            contentType = APPLICATION_JSON
            accept = APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }
    }

    test("reserve book should return 409 when book already reserved") {
        every { bookUseCase.reserveBook(1) } throws IllegalStateException("Book already reserved")

        mockMvc.post("/books/reserved/1") {
            contentType = APPLICATION_JSON
            accept = APPLICATION_JSON
        }.andExpect {
            status { isConflict() }
        }
    }
})