package dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val email: String, val password: String, val displayName: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val token: String)

@Serializable
data class UpdateProgressRequest(val position: String, val percentage: Float)

@Serializable
data class CreateBookmarkRequest(val position: String, val note: String? = null)

@Serializable
data class AddToLibraryRequest(val bookId: String)