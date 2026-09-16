package com.example.prontocheck.ui.AltaEmpleado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.prontocheck.data.repository.UserRepository

class AltaEmpleadoViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AltaEmpleadoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AltaEmpleadoViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}