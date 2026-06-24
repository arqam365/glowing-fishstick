package com.revzion.siitglobe.data.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class TokenStorage {
    private val settings: Settings = Settings()

    fun getToken(): String? = settings.getStringOrNull(KEY_TOKEN)

    fun setToken(token: String) {
        settings[KEY_TOKEN] = token
    }

    fun clear() {
        settings.remove(KEY_TOKEN)
    }

    companion object {
        private const val KEY_TOKEN = "auth_token"
    }
}
