package com.example.prontocheck.ui.Incidencias

import com.example.prontocheck.data.model.Incidencia
import com.example.prontocheck.data.network.SupabaseApi

/**
 * Repository de incidencias.
 *
 * Por que existe: separa las llamadas a Supabase de la pantalla y del ViewModel.
 * Ventaja: si cambia la API, solo se toca esta clase; la UI sigue observando el
 * mismo ViewModel y no queda mezclada con detalles de red como filtros "eq.*".
 */
class IncidenciasRepository(private val api: SupabaseApi) {

    suspend fun obtenerIncidencias() = api.getIncidencias()

    suspend fun buscarEmpleadoPorCodigo(codigoEmpleado: String) =
        api.getEmpleadoPorCodigo("eq.$codigoEmpleado")

    suspend fun registrarIncidencia(incidencia: Incidencia) =
        api.registrarIncidencia(incidencia)

    suspend fun actualizarEstado(idIncidencia: Long, nuevoEstado: String) =
        api.actualizarEstadoIncidencias(
            idFilter = "eq.$idIncidencia",
            campos = mapOf("estado" to nuevoEstado)
        )
}
