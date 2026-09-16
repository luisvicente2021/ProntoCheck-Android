package com.example.prontocheck.ui.gestionUbicaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory del ViewModel de ubicaciones.
 *
 * Por que existe: permite crear GestionUbicacionesViewModel con su Repository
 * sin acoplar el ViewModel al singleton de Retrofit.
 *
 * Ventaja: deja la dependencia visible desde la Activity y facilita migrar luego
 * a Hilt/Koin si el proyecto crece.
 */
class GestionUbicacionesViewModelFactory(
    private val repository: GestionUbicacionesRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionUbicacionesViewModel::class.java)) {
            return GestionUbicacionesViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
