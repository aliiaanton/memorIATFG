package com.memoria.app.data

import android.content.Context

class AuthSessionStore(context: Context) {

    private val preferences = context.getSharedPreferences("memoria_auth", Context.MODE_PRIVATE)

    fun load(): AuthSession? {
        val accessToken = preferences.getString(KEY_ACCESS_TOKEN, null)?.takeIf { it.isNotBlank() }
            ?: return null
        return AuthSession(
            accessToken = accessToken,
            refreshToken = preferences.getString(KEY_REFRESH_TOKEN, null),
            userId = preferences.getString(KEY_USER_ID, null),
            email = preferences.getString(KEY_EMAIL, null)
        )
    }

    fun save(session: AuthSession) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, session.accessToken)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_EMAIL, session.email)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
    }
}
