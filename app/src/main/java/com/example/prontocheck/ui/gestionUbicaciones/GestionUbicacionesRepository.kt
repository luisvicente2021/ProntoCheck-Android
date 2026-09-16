package com.example.prontocheck.ui.gestionUbicaciones

import com.example.prontocheck.data.model.PuntoAcceso
import com.example.prontocheck.data.network.SupabaseApi

/**
 * Repository de puntos de acceso.
 *
 * Por que existe: encapsula las operaciones de Supabase para ubicaciones y deja
 * fuera de la pantalla los detalles de endpoints, filtros y cuerpos de request.
 *
 * Ventaja: el Activity ya no depende directamente de Retrofit y se vuelve mas
 * facil cambiar Supabase, simular datos o probar la logica desde el ViewModel.
 */
class GestionUbicacionesRepository(private val api: SupabaseApi) {

    suspend fun obtenerPuntos() = api.getPuntosAcceso()

    suspend fun guardarPunto(punto: PuntoAcceso) =
        api.registrarPuntoAcceso(punto)

    suspend fun eliminarPunto(id: String) =
        api.eliminarPuntoAcceso("eq.$id")
}
