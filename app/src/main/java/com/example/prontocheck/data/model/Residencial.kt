package com.example.prontocheck.data.model

import com.google.gson.annotations.SerializedName

data class Residencial(

    @SerializedName("id")
    val id: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("activo")
    val activo: Boolean = true
)