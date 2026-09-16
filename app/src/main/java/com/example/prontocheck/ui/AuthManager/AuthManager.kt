package com.example.prontocheck.ui.AuthManager

import android.content.Context
import android.content.Intent
import com.example.prontocheck.ui.login.LoginActivity

object AuthManager {
    fun cerrarSesion(context: Context) {
        // 1. Borrar SharedPreferences
        val prefs = context.getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // 2. Ir al Login y limpiar el historial de pantallas
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}