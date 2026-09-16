package com.example.prontocheck.data.model

import com.google.gson.annotations.SerializedName

data class PuntoAcceso(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("nombre_residencial")
    val nombre_residencial: String,

    @SerializedName("residencial_id")
    val residencial_id: String?, // Agregado como opcional

    @SerializedName("latitud")
    val latitud: Double,

    @SerializedName("longitud")
    val longitud: Double,

    @SerializedName("radio_metros")
    val radio_metros: Double = 60.0,

    @SerializedName("activo")
    val activo: Boolean = true
)