package com.example.prontocheck

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun fetchToken(): String? = prefs.getString("auth_token", null)
}