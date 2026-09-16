package com.example.prontocheck.data.repository

import com.example.prontocheck.data.model.Empleado
import com.example.prontocheck.data.network.SupabaseApi

/**
 * Repository central para empleados.
 *
 * Por que existe: varias pantallas necesitan consultar o modificar empleados.
 * Centralizarlo evita duplicar filtros de Supabase en cada Activity/ViewModel.
 *
 * Ventaja: el acceso a datos queda en una sola capa y las pantallas trabajan con
 * operaciones de negocio mas legibles: listar, actualizar, eliminar.
 */
class EmpleadoRepository(private val api: SupabaseApi) {

    suspend fun obtenerActivos(limit: Int, offset: Int) =
        api.getEmpleadosRegistro(
            activo = "eq.true",
            limit = limit,
            offset = offset
        )

    suspend fun obtenerPorResidencial(residencial: String, limit: Int = 100, offset: Int = 0) =
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
