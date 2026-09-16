package com.example.prontocheck.ui.Incidencias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory del ViewModel de incidencias.
 *
 * Por que existe: IncidenciasViewModel necesita un Repository en el constructor,
 * y el delegado normal de Android solo sabe crear ViewModels sin parametros.
 *
 * Ventaja: mantiene la inyeccion de dependencias simple y explicita sin meter un
 * framework adicional todavia.
 */
class IncidenciasViewModelFactory(
    private val repository: IncidenciasRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncidenciasViewModel::class.java)) {
            return IncidenciasViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
