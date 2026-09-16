package com.example.prontocheck.ui.cuadrante

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory del ViewModel de cuadrante.
 *
 * Por que existe: permite inyectar CuadranteRepository sin crear dependencias
 * globales dentro del ViewModel.
 *
 * Ventaja: mantiene la creacion simple ahora y deja listo el camino para DI.
 */
class CuadranteViewModelFactory(
    private val repository: CuadranteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CuadranteViewModel::class.java)) {
            return CuadranteViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
