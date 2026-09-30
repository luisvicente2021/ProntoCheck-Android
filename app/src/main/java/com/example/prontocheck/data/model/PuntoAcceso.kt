package com.example.prontocheck.data.model

import com.google.gson.annotations.SerializedName

data class PuntoAcceso(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("residencial_id")
    val residencialId: String,

    @SerializedName("nombre_residencial")
    val nombreResidencial: String? = null,

    @SerializedName("nombre_punto")
    val nombrePunto: String,

    @SerializedName("latitud")
    val latitud: Double,

    @SerializedName("longitud")
    val longitud: Double,

    @SerializedName("radio_metros")
    val radioMetros: Double = 200.0,

    @SerializedName("activo")
    val activo: Boolean = true
)