package com.example.prontocheck.ui.gestionUbicaciones

import com.example.prontocheck.data.model.PuntoAcceso
import com.example.prontocheck.data.network.SupabaseApi

class GestionUbicacionesRepository(
    private val api: SupabaseApi
) {

    suspend fun obtenerPuntos() =
        api.getPuntosAcceso()

    suspend fun obtenerResidenciales() =
        api.getResidenciales()

    suspend fun guardarPunto(
        punto: PuntoAcceso
    ) =
        api.registrarPuntoAcceso(punto)

    suspend fun eliminarPunto(
        id: String
    ) =
        api.actualizarPuntoAcceso(
            "eq.$id",
            mapOf("activo" to false)
        )
}