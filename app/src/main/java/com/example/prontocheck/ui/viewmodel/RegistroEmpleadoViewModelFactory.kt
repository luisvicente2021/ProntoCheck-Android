package com.example.prontocheck.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.prontocheck.data.repository.EmpleadoRepository

/**
 * Factory de RegistroEmpleadoViewModel.
 *
 * Por que existe: el ViewModel necesita EmpleadoRepository para actualizar y
 * eliminar empleados.
 *
 * Ventaja: la Activity no construye logica de red y el ViewModel no depende
 * directamente de Retrofit.
 */
class RegistroEmpleadoViewModelFactory(
    private val repository: EmpleadoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistroEmpleadoViewModel::class.java)) {
            return RegistroEmpleadoViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
