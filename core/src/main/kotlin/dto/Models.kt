package dto

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class User(
    val id: UUID,
    val email: String,
    val displayName: String,
)

@Serializable
data class Book(
    val id: UUID,
    val title: String,
    val author: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val format: String = "epub",
)

@Serializable
data class NewBook(
    val title: String,
    val author: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val filePath: String,
    val format: String = "epub",
    val uploadedBy: UUID? = null,
)

@Serializable
data class LibraryEntry(
    val id: UUID,
    val userId: UUID,
    val bookId: UUID,
    val addedAt: String,
)

@Serializable
data class ReadingProgress(
    val userId: UUID,
    val bookId: UUID,
    val position: String,
    val percentage: Float,
    val lastReadAt: String,
)

@Serializable
data class Bookmark(
    val id: UUID,
    val userId: UUID,
    val bookId: UUID,
    val position: String,
    val note: String? = null,
    val createdAt: String,
)