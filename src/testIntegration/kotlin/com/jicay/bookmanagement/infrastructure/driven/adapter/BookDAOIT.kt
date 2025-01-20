package com.jicay.bookmanagement.infrastructure.driven.adapter

import com.jicay.bookmanagement.domain.model.Book
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.ResultSet

@SpringBootTest
@ActiveProfiles("testIntegration")
class BookDAOIT(
    private val bookDAO: BookDAO
) : FunSpec() {
    init {
        extension(SpringExtension)

        beforeTest {
            performQuery(
                // language=sql
                "DELETE FROM book"
            )
        }

        test("get all books from db") {
            performQuery(
                // language=sql
                """
               insert into book (title, author, reserved)
               values 
                   ('Hamlet', 'Shakespeare', false),
                   ('Les fleurs du mal', 'Beaudelaire', false),
                   ('Harry Potter', 'Rowling', false);
            """.trimIndent()
            )

            val res = bookDAO.getAllBooks()

            res.shouldContainExactlyInAnyOrder(
                Book("Hamlet", "Shakespeare", false), Book("Les fleurs du mal", "Beaudelaire", false), Book("Harry Potter", "Rowling", false)
            )
        }

        test("create book in db") {
            bookDAO.createBook(Book("Les misérables", "Victor Hugo"))

            val res = performQuery(
                // language=sql
                "SELECT * from book"
            )

            res shouldHaveSize 1
            assertSoftly(res.first()) {
                this["id"].shouldNotBeNull().shouldBeInstanceOf<Int>()
                this["title"].shouldBe("Les misérables")
                this["author"].shouldBe("Victor Hugo")
            }
        }

        test("get book by id should return book when it exists") {
            performQuery(
                // language=sql
            """
               insert into book (id, title, author, reserved)
               values (1, 'Les misérables', 'Victor Hugo', false)
            """.trimIndent()
            )

            val res = bookDAO.getBook(1)

            res.shouldNotBeNull()
            assertSoftly(res) {
                name shouldBe "Les misérables"
                author shouldBe "Victor Hugo"
                reserved shouldBe false
            }
        }

        test("get book by id should return null when book doesn't exist") {
            val res = bookDAO.getBook(1)

            res.shouldBeNull()
        }

        test("reserve book should update reserved status") {
            performQuery(
                // language=sql
                """
                   insert into book (id, title, author, reserved)
                   values (1, 'Les misérables', 'Victor Hugo', false)
                """.trimIndent()
            )

            bookDAO.reserveBook(1)

            val res = performQuery(
                // language=sql
                "SELECT * from book WHERE id = 1"
            )

            res shouldHaveSize 1
            assertSoftly(res.first()) {
                this["reserved"].shouldBe(true)
            }
        }

        test("reserving an already reserved book should not alter the record") {
            performQuery(
                // language=sql
                """
        INSERT INTO book (id, title, author, reserved)
        VALUES (1, 'Les Misérables', 'Victor Hugo', true)
        """.trimIndent()
            )

            bookDAO.reserveBook(1)

            val res = performQuery("SELECT * FROM book WHERE id = 1")

            res shouldHaveSize 1
            assertSoftly(res.first()) {
                this["reserved"].shouldBe(true) // Réservé reste vrai
            }
        }

        test("reserving a non-existent book should not create new records") {
            bookDAO.reserveBook(999)

            val res = performQuery(
                // language=sql
                "SELECT * FROM book WHERE id = 999"
            )

            res shouldHaveSize 0
        }

        afterSpec {
            container.stop()
        }
    }



    companion object {
        private val container = PostgreSQLContainer<Nothing>("postgres:13-alpine")

        init {
            container.start()
            System.setProperty("spring.datasource.url", container.jdbcUrl)
            System.setProperty("spring.datasource.username", container.username)
            System.setProperty("spring.datasource.password", container.password)
        }

        private fun ResultSet.toList(): List<Map<String, Any>> {
            val md = this.metaData
            val columns = md.columnCount
            val rows: MutableList<Map<String, Any>> = ArrayList()
            while (this.next()) {
                val row: MutableMap<String, Any> = HashMap(columns)
                for (i in 1..columns) {
                    row[md.getColumnName(i)] = this.getObject(i)
                }
                rows.add(row)
            }
            return rows
        }

        fun performQuery(sql: String): List<Map<String, Any>> {
            val hikariConfig = HikariConfig()
            hikariConfig.setJdbcUrl(container.jdbcUrl)
            hikariConfig.username = container.username
            hikariConfig.password = container.password
            hikariConfig.setDriverClassName(container.driverClassName)

            val ds = HikariDataSource(hikariConfig)

            val statement = ds.connection.createStatement()
            statement.execute(sql)
            val resultSet = statement.resultSet
            return resultSet?.toList() ?: listOf()
        }
    }
}