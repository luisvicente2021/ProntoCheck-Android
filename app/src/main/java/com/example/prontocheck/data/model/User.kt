package com.example.prontocheck.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    // Este es el campo clave para los roles
    @SerializedName("rol") val rol: String? = "SECURITY"
)