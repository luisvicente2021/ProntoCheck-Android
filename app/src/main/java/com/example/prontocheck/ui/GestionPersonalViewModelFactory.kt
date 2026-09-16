package com.example.prontocheck.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.prontocheck.data.repository.EmpleadoRepository

/**
 * Factory de GestionPersonalViewModel.
 *
 * Por que existe: el ViewModel recibe un Repository por constructor.
 * Ventaja: permite dependencias explicitas sin acoplar el ViewModel a Retrofit.
 */
class GestionPersonalViewModelFactory(
    private val repository: EmpleadoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionPersonalViewModel::class.java)) {
            return GestionPersonalViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
