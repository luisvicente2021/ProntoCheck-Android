package com.example.prontocheck.data.model

import com.google.gson.annotations.SerializedName


data class Asistencia(
    @SerializedName("empleado_id")
    val idEmpleadoManual: String,

    @SerializedName("nombre_empleado")
    val nombreEmpleado: String,

    @SerializedName("tipo")
    val tipo: String,  // "entrada" o "salida"

    @SerializedName("fecha")
    val fecha: String,

    @SerializedName("hora")
    val hora: String,

    @SerializedName("id_punto_acceso")
    val idPuntoAcceso: String? = null
)