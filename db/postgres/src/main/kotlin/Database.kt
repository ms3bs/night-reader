package db.postgres

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun initDatabase(
    url: String = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/web_reader",
    user: String = System.getenv("DB_USER") ?: "reader",
    password: String = System.getenv("DB_PASSWORD") ?: "reader_pass",
): Database {
    val cfg = HikariConfig().apply {
        jdbcUrl = url
        username = user
        this.password = password
        maximumPoolSize = 10
        driverClassName = "org.postgresql.Driver"
    }
    val db = Database.connect(HikariDataSource(cfg))
    transaction(db) {
        SchemaUtils.create(UsersTable, BooksTable, LibraryTable, ProgressTable, BookmarksTable)
    }
    return db
}