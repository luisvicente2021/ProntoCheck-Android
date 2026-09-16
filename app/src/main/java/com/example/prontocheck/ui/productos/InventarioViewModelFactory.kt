package com.example.prontocheck.ui.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory de InventarioViewModel.
 *
 * Por que existe: InventarioViewModel necesita su Repository para consultar y
 * actualizar productos.
 *
 * Ventaja: elimina factories anonimas en la Activity y deja el patron igual al
 * resto de pantallas MVVM.
 */
class InventarioViewModelFactory(
    private val repository: InventarioRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventarioViewModel::class.java)) {
            return InventarioViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
