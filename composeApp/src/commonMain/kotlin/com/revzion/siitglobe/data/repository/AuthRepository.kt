package com.revzion.siitglobe.data.repository

import com.revzion.siitglobe.data.api.AuthApi
import com.revzion.siitglobe.data.model.User
import com.revzion.siitglobe.data.storage.TokenStorage

class AuthRepository(
    private val api: AuthApi,
    private val storage: TokenStorage,
) {
    fun isLoggedIn(): Boolean = storage.getToken() != null

    fun getToken(): String? = storage.getToken()

    suspend fun signIn(email: String, password: String): Result<User> {
        val result = api.signIn(email, password)
        return result.map { response ->
            storage.setToken(response.token)
            response.user
        }
    }

    suspend fun signUp(name: String, email: String, password: String): Result<User> {
        val result = api.signUp(name, email, password)
        return result.map { response ->
            storage.setToken(response.token)
            response.user
        }
    }

    suspend fun getUser(): Result<User> {
        val token = storage.getToken() ?: return Result.failure(Exception("Not authenticated"))
        return api.getUser(token).map { it.user }
    }

    suspend fun signOut() {
        val token = storage.getToken()
        if (token != null) {
            api.signOut(token)
        }
        storage.clear()
    }
}
