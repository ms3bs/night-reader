package db.postgres

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : UUIDTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val displayName = varchar("display_name", 100)
    val createdAt = timestamp("created_at")
}

object BooksTable : UUIDTable("books") {
    val title = varchar("title", 500)
    val author = varchar("author", 300)
    val description = text("description").nullable()
    val coverUrl = text("cover_url").nullable()
    val filePath = text("file_path")
    val format = varchar("format", 10).default("epub")
    val uploadedBy = uuid("uploaded_by").nullable()
    val createdAt = timestamp("created_at")
}

object LibraryTable : UUIDTable("library_entries") {
    val userId = uuid("user_id")
    val bookId = uuid("book_id")
    val addedAt = timestamp("added_at")
}

object ProgressTable : UUIDTable("reading_progress") {
    val userId = uuid("user_id")
    val bookId = uuid("book_id")
    val position = varchar("position", 500)
    val percentage = float("percentage").default(0f)
    val lastReadAt = timestamp("last_read_at")
}

object BookmarksTable : UUIDTable("bookmarks") {
    val userId = uuid("user_id")
    val bookId = uuid("book_id")
    val position = varchar("position", 500)
    val note = text("note").nullable()
    val createdAt = timestamp("created_at")
}