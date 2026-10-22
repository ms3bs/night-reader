package db.postgres

import dto.*
import exception.ConflictException
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.mindrot.jbcrypt.BCrypt
import repository.*
import java.time.Instant
import java.util.UUID

private suspend fun <T> tx(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }

class PostgresUserRepository : UserRepository {
    override suspend fun create(email: String, password: String, displayName: String): User = tx {
        val exists = UsersTable.selectAll().where { UsersTable.email eq email }.any()
        if (exists) throw ConflictException("Email already registered")
        val id = UUID.randomUUID()
        UsersTable.insert {
            it[UsersTable.id] = id
            it[UsersTable.email] = email
            it[passwordHash] = BCrypt.hashpw(password, BCrypt.gensalt())
            it[UsersTable.displayName] = displayName
            it[createdAt] = Instant.now()
        }
        User(id, email, displayName)
    }

    override suspend fun findByEmail(email: String): Pair<User, String>? = tx {
        UsersTable.selectAll().where { UsersTable.email eq email }
            .map { row ->
                User(row[UsersTable.id].value, row[UsersTable.email], row[UsersTable.displayName]) to
                    row[UsersTable.passwordHash]
            }.singleOrNull()
    }

    override suspend fun findById(id: UUID): User? = tx {
        UsersTable.selectAll().where { UsersTable.id eq id }
            .map { User(it[UsersTable.id].value, it[UsersTable.email], it[UsersTable.displayName]) }
            .singleOrNull()
    }
}

class PostgresBookRepository : BookRepository {
    private fun ResultRow.toBook() = Book(
        id = this[BooksTable.id].value,
        title = this[BooksTable.title],
        author = this[BooksTable.author],
        description = this[BooksTable.description],
        coverUrl = this[BooksTable.coverUrl],
        format = this[BooksTable.format],
    )

    override suspend fun findAll(page: Int, size: Int): List<Book> = tx {
        BooksTable.selectAll()
            .orderBy(BooksTable.createdAt, SortOrder.DESC)
            .limit(size).offset((page * size).toLong())
            .map { it.toBook() }
    }

    override suspend fun findById(id: UUID): Book? = tx {
        BooksTable.selectAll().where { BooksTable.id eq id }.map { it.toBook() }.singleOrNull()
    }

    override suspend fun search(query: String): List<Book> = tx {
        BooksTable.selectAll()
            .where { (BooksTable.title like "%$query%") or (BooksTable.author like "%$query%") }
            .map { it.toBook() }
    }

    override suspend fun create(book: NewBook): Book = tx {
        val id = UUID.randomUUID()
        BooksTable.insert {
            it[BooksTable.id] = id
            it[title] = book.title
            it[author] = book.author
            it[description] = book.description
            it[coverUrl] = book.coverUrl
            it[filePath] = book.filePath
            it[format] = book.format
            it[uploadedBy] = book.uploadedBy
            it[createdAt] = Instant.now()
        }
        Book(id, book.title, book.author, book.description, book.coverUrl, book.format)
    }
}

class PostgresLibraryRepository : LibraryRepository {
    override suspend fun getUserLibrary(userId: UUID): List<Pair<LibraryEntry, Book>> = tx {
        (LibraryTable innerJoin BooksTable)
            .selectAll().where { LibraryTable.userId eq userId }
            .map { row ->
                val entry = LibraryEntry(
                    id = row[LibraryTable.id].value,
                    userId = row[LibraryTable.userId],
                    bookId = row[LibraryTable.bookId],
                    addedAt = row[LibraryTable.addedAt].toString(),
                )
                val book = Book(
                    id = row[BooksTable.id].value,
                    title = row[BooksTable.title],
                    author = row[BooksTable.author],
                    description = row[BooksTable.description],
                    coverUrl = row[BooksTable.coverUrl],
                    format = row[BooksTable.format],
                )
                entry to book
            }
    }

    override suspend fun add(userId: UUID, bookId: UUID): LibraryEntry = tx {
        val exists = LibraryTable.selectAll()
            .where { (LibraryTable.userId eq userId) and (LibraryTable.bookId eq bookId) }.any()
        if (exists) throw ConflictException("Already in library")
        val id = UUID.randomUUID()
        val now = Instant.now()
        LibraryTable.insert {
            it[LibraryTable.id] = id
            it[LibraryTable.userId] = userId
            it[LibraryTable.bookId] = bookId
            it[addedAt] = now
        }
        LibraryEntry(id, userId, bookId, now.toString())
    }

    override suspend fun remove(userId: UUID, bookId: UUID) {
        tx {
            LibraryTable.deleteWhere {
                (LibraryTable.userId eq userId) and (LibraryTable.bookId eq bookId)
            }
        }
    }
}

class PostgresReadingProgressRepository : ReadingProgressRepository {
    override suspend fun find(userId: UUID, bookId: UUID): ReadingProgress? = tx {
        ProgressTable.selectAll()
            .where { (ProgressTable.userId eq userId) and (ProgressTable.bookId eq bookId) }
            .map { row ->
                ReadingProgress(
                    userId = row[ProgressTable.userId],
                    bookId = row[ProgressTable.bookId],
                    position = row[ProgressTable.position],
                    percentage = row[ProgressTable.percentage],
                    lastReadAt = row[ProgressTable.lastReadAt].toString(),
                )
            }.singleOrNull()
    }

    override suspend fun upsert(userId: UUID, bookId: UUID, position: String, percentage: Float): ReadingProgress = tx {
        val now = Instant.now()
        val existing = ProgressTable.selectAll()
            .where { (ProgressTable.userId eq userId) and (ProgressTable.bookId eq bookId) }
            .any()
        if (existing) {
            ProgressTable.update({
                (ProgressTable.userId eq userId) and (ProgressTable.bookId eq bookId)
            }) {
                it[ProgressTable.position] = position
                it[ProgressTable.percentage] = percentage
                it[lastReadAt] = now
            }
        } else {
            ProgressTable.insert {
                it[id] = UUID.randomUUID()
                it[ProgressTable.userId] = userId
                it[ProgressTable.bookId] = bookId
                it[ProgressTable.position] = position
                it[ProgressTable.percentage] = percentage
                it[lastReadAt] = now
            }
        }
        ReadingProgress(userId, bookId, position, percentage, now.toString())
    }
}

class PostgresBookmarkRepository : BookmarkRepository {
    private fun ResultRow.toBookmark() = Bookmark(
        id = this[BookmarksTable.id].value,
        userId = this[BookmarksTable.userId],
        bookId = this[BookmarksTable.bookId],
        position = this[BookmarksTable.position],
        note = this[BookmarksTable.note],
        createdAt = this[BookmarksTable.createdAt].toString(),
    )

    override suspend fun list(userId: UUID, bookId: UUID): List<Bookmark> = tx {
        BookmarksTable.selectAll()
            .where { (BookmarksTable.userId eq userId) and (BookmarksTable.bookId eq bookId) }
            .map { it.toBookmark() }
    }

    override suspend fun create(userId: UUID, bookId: UUID, position: String, note: String?): Bookmark = tx {
        val id = UUID.randomUUID()
        val now = Instant.now()
        BookmarksTable.insert {
            it[BookmarksTable.id] = id
            it[BookmarksTable.userId] = userId
            it[BookmarksTable.bookId] = bookId
            it[BookmarksTable.position] = position
            it[BookmarksTable.note] = note
            it[createdAt] = now
        }
        Bookmark(id, userId, bookId, position, note, now.toString())
    }

    override suspend fun delete(userId: UUID, id: UUID) {
        tx { BookmarksTable.deleteWhere { (BookmarksTable.userId eq userId) and (BookmarksTable.id eq id) } }
    }
}