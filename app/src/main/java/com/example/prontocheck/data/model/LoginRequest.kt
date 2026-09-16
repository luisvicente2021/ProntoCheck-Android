package com.example.prontocheck.data.model


import com.google.gson.annotations.SerializedName

// Lo que envías
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

// Lo que Supabase REALMENTE devuelve cuando el login es exitoso
data class LoginResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("expires_in") val expiresIn: Int?,
    @SerializedName("user") val user: SupabaseUser?
)

data class SupabaseUser(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    // AGREGAMOS ESTA LÍNEA: Conecta al usuario con sus metadatos (donde vive el ROL)
    @SerializedName("user_metadata") val metadata: UserMetadata?
)


data class UserMetadata(
    // Definimos el campo rol que configuraremos en la base de datos
    // Puede ser: "ADMIN", "RECURSOS_HUMANOS", "SEGURIDAD"
    @SerializedName("rol") val rol: String?
)