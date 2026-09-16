package com.example.prontocheck.data.model


import com.google.gson.annotations.SerializedName


data class Empleado(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("apellido_paterno")
    val apellido_paterno: String? = null,
    @SerializedName("apellido_materno")
    val apellido_materno: String? = null,
    @SerializedName("residencial")
    val residencial: String? = null,
    @SerializedName("direccion")
    val direccion: String? = null,
    @SerializedName("telefono_casa")
    val telefono_casa: String? = null,
    @SerializedName("telefono_empresa")
    val telefono_empresa: String? = null,
    @SerializedName("latitud")
    val latitud: Double? = 0.0,
    @SerializedName("longitud")
    val longitud: Double? = 0.0,
    @SerializedName("face_embedding")
    val face_embedding: String? = null,
    @SerializedName("activo")
    val activo: Boolean = true,
    @SerializedName("jornada_horas")
    val jornada_horas: Int = 8
)
