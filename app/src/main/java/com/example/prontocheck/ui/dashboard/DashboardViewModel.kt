package com.example.prontocheck.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.prontocheck.data.repository.UserRepository


// Agrega (private val repository: UserRepository) aquí:
class DashboardViewModel(private val repository: UserRepository) : ViewModel() {

    private val _usuarioNombre = MutableLiveData<String>()
    val usuarioNombre: LiveData<String> get() = _usuarioNombre

    private val _cerrarSesionEvento = MutableLiveData<Boolean>()
    val cerrarSesionEvento: LiveData<Boolean> get() = _cerrarSesionEvento

    init {
        // Por ahora dejamos esto estático,
        // luego usaremos el repository para traer el nombre real
        _usuarioNombre.value = "Administrador"
    }

    fun cerrarSesion() {
        _cerrarSesionEvento.value = true
    }
}