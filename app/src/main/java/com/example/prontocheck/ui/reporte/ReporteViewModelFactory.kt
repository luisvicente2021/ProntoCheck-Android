package com.example.prontocheck.ui.reporte


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.prontocheck.data.repository.UserRepository

class ReporteViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReporteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReporteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}