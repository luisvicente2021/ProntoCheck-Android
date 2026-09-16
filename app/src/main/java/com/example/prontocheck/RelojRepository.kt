package com.example.prontocheck

import com.example.prontocheck.data.model.Asistencia
import com.example.prontocheck.data.network.SupabaseApi

/**
 * Repository del reloj checador.
 *
 * Por que existe: extrae las llamadas a Supabase de RelojActivity, que ya tiene
 * suficiente responsabilidad con camara, permisos, GPS y reconocimiento facial.
 *
 * Ventaja: la Activity deja de depender de Retrofit y el registro de asistencia
 * queda aislado para cambiarlo o probarlo con menos riesgo.
 */
class RelojRepository(private val api: SupabaseApi) {

    suspend fun obtenerPuntosAcceso() = api.getPuntosAcceso()

    suspend fun obtenerEmpleadosConRostro() =
        api.getEmpleadosRegistro(limit = 1000, offset = 0, activo = "eq.true")

    suspend fun registrarAsistencia(asistencia: Asistencia) =
        api.registrarAsistencia(asistencia)

    suspend fun obtenerUltimaAsistencia(empleadoId: String) =
        api.getUltimaAsistencia(
            empleadoId = "eq.$empleadoId"
        )
}
