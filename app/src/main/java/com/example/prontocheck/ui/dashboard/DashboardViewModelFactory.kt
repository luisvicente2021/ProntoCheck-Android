package com.example.prontocheck.ui.dashboard


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.prontocheck.data.repository.UserRepository

/**
 * Factory de DashboardViewModel.
 *
 * Por que existe: mantiene la creacion del ViewModel con dependencias explicitas.
 * Ventaja: DashboardActivity no necesita saber como se crea UserRepository.
 */
class DashboardViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
