package com.revzion.siitglobe.data.api

import com.revzion.siitglobe.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

private const val BASE_URL = "https://api.siit.net.in"

class AuthApi(private val client: HttpClient) {

    suspend fun signIn(email: String, password: String): Result<AuthResponse> = runCatching {
        client.post("$BASE_URL/api/auth/sign-in/email") {
            contentType(ContentType.Application.Json)
            setBody(SignInRequest(email, password))
        }.body()
    }

    suspend fun signUp(name: String, email: String, password: String): Result<AuthResponse> = runCatching {
        client.post("$BASE_URL/api/auth/sign-up/email") {
            contentType(ContentType.Application.Json)
            setBody(SignUpRequest(name, email, password))
        }.body()
    }

    suspend fun getUser(token: String): Result<UserResponse> = runCatching {
        client.get("$BASE_URL/api/user") {
            bearerAuth(token)
        }.body()
    }

    suspend fun signOut(token: String): Result<Unit> = runCatching {
        client.post("$BASE_URL/api/auth/sign-out") {
            bearerAuth(token)
        }
        Unit
    }
}
