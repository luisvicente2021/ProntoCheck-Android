package com.example.prontocheck.data.model

data class Incidencia(
    val id: Long? = null,
    val empleado_id: String, // Cambiado de Long a String para el UUID
    val tipo: String,
    val motivo: String?,
    val estado: String = "Pendiente",
    val evidencia_url: String? = null,
    val fecha_incidencia: String? = null,
    val nombre_empleado: String? = null
)