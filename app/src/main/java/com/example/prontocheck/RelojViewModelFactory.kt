package com.example.prontocheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory del ViewModel del reloj.
 *
 * Por que existe: RelojViewModel necesita un Repository por constructor.
 * Ventaja: evita crear Retrofit dentro del ViewModel y conserva dependencias
 * explicitas.
 */
class RelojViewModelFactory(
    private val repository: RelojRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RelojViewModel::class.java)) {
            return RelojViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
