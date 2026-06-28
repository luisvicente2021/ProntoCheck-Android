package com.example.prontocheck.data.repository

import com.example.prontocheck.data.model.Empleado
import com.example.prontocheck.data.network.SupabaseApi

class EmpleadoRepository(private val api: SupabaseApi) {

    suspend fun crear(empleado: Empleado) =
        api.registrarEmpleado(empleado)

    suspend fun obtenerActivos(limit: Int, offset: Int) =
        api.getEmpleadosRegistro(
            activo = "eq.true",
            limit = limit,
            offset = offset
        )

    suspend fun obtenerPorResidencial(
        residencial: String,
        limit: Int = 100,
        offset: Int = 0
    ) =
        api.getEmpleadosPorResidencial(
            residencial = "eq.$residencial",
            activo = "eq.true",
            limit = limit,
            offset = offset
        )

    suspend fun actualizar(id: String, empleado: Empleado) =
        api.actualizarEmpleado("eq.$id", empleado)

    suspend fun eliminar(id: String) =
        api.eliminarFisicamente("eq.$id")
}