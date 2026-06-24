package com.revzion.siitglobe.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("emailVerified") val emailVerified: Boolean = false,
    val image: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class Session(
    val id: String,
    val token: String,
    val expiresAt: String,
    val userId: String,
)

@Serializable
data class AuthResponse(
    val user: User,
    val token: String,
)

@Serializable
data class UserResponse(
    val user: User,
)

@Serializable
data class SignInRequest(
    val email: String,
    val password: String,
)

@Serializable
data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String,
)

@Serializable
data class ApiError(
    val message: String,
    val code: String? = null,
)
