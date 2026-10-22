package repository

import dto.*
import java.util.UUID

interface UserRepository {
    suspend fun create(email: String, password: String, displayName: String): User
    suspend fun findByEmail(email: String): Pair<User, String>?
    suspend fun findById(id: UUID): User?
}

interface BookRepository {
    suspend fun findAll(page: Int, size: Int): List<Book>
    suspend fun findById(id: UUID): Book?
    suspend fun search(query: String): List<Book>
    suspend fun create(book: NewBook): Book
}

interface LibraryRepository {
    suspend fun getUserLibrary(userId: UUID): List<Pair<LibraryEntry, Book>>
    suspend fun add(userId: UUID, bookId: UUID): LibraryEntry
    suspend fun remove(userId: UUID, bookId: UUID)
}

interface ReadingProgressRepository {
    suspend fun find(userId: UUID, bookId: UUID): ReadingProgress?
    suspend fun upsert(userId: UUID, bookId: UUID, position: String, percentage: Float): ReadingProgress
}

interface BookmarkRepository {
    suspend fun list(userId: UUID, bookId: UUID): List<Bookmark>
    suspend fun create(userId: UUID, bookId: UUID, position: String, note: String?): Bookmark
    suspend fun delete(userId: UUID, id: UUID)
}