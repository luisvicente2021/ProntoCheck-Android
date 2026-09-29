package com.example.prontocheck

import android.content.Context

class SessionManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit()
            .putString("TOKEN", token)
            .apply()
    }

    fun fetchToken(): String? {
        return prefs.getString("TOKEN", null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}