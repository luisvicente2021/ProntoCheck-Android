package com.example.prontocheck.data.repository

import com.example.prontocheck.data.model.*
import com.example.prontocheck.data.network.SupabaseApi
import retrofit2.Response

/**
 * Repository de usuarios, autenticacion y reportes historicos.
 *
 * Por que existe: agrupa operaciones usadas por login, alta, dashboard y
 * reportes mientras el proyecto termina de separar repositories por dominio.
 *
 * Ventaja: las pantallas y ViewModels no conocen endpoints ni filtros de
 * Supabase directamente.
 */
class UserRepository(private val apiService: SupabaseApi) {

    // --- AUTENTICACIÓN ---
    suspend fun login(email: String, pass: String): Response<LoginResponse> {
        val request = LoginRequest(email, pass)
        return apiService.loginUsuario(request)
    }
    
    suspend fun getEmpleados(): Response<List<Empleado>> {
        return apiService.getEmpleados()
    }

    suspend fun registrarEmpleado(empleado: Empleado, token: String): Response<Unit> {
        return apiService.registrarEmpleado(
            empleado = empleado
        )
    }

    suspend fun actualizarEmpleado(id: String, empleado: Empleado): Response<Unit> {
        return apiService.actualizarEmpleado("eq.$id", empleado)
    }

    // --- ASISTENCIA ---

    suspend fun registrarAsistencia(asistencia: Asistencia): Response<Unit> {
        return apiService.registrarAsistencia(asistencia)
    }

    suspend fun getAsistencias(fechaInicio: String, fechaFin: String): Response<List<Asistencia>> {
        return apiService.getAsistenciaPorPeriodo("gte.$fechaInicio", "lte.$fechaFin")
    }

    suspend fun signUp(email: String, pass: String): Response<LoginResponse> {
        return apiService.registrarEnAuth(LoginRequest(email, pass))
    }

    suspend fun obtenerReporteAsistencias(fInicio: String, fFin: String, residencial: String): Response<List<ResumenEmpleado>> {
        val queryInicio = "gte.$fInicio"
        val queryFin = "lte.$fFin"

        return if (residencial == "Todas") {
            apiService.getReporte(queryInicio, queryFin)
        } else {
            apiService.getReporte(queryInicio, queryFin, "eq.$residencial")
        }
    }
}
