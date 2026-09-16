package com.example.prontocheck.data.model

data class ResumenEmpleado(
    val nombre: String,
    val residencial: String,
    val diasAsistidos: Int,
    val totalHoras: String, // Se recibe como String desde la API/ViewModel
    val detalles: List<String> = emptyList() // Lista de fechas y horas individuales
)
