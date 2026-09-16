package com.example.prontocheck.ui.reporte

data class ResumenEmpleado(
    val idEmpleado: String,
    val nombre: String,
    val residencial: String,
    val diasAsistidos: Int,
    val detalles: List<String>,
    val totalHoras: String
)