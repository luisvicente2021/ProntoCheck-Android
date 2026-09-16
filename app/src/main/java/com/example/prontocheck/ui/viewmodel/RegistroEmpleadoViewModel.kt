package com.example.prontocheck.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prontocheck.data.model.Empleado
import com.example.prontocheck.data.repository.EmpleadoRepository
import com.example.prontocheck.utils.Resource
import kotlinx.coroutines.launch


/**
 * ViewModel para editar/eliminar empleados.
 *
 * Por que existe: concentra las acciones de escritura de un empleado fuera de
 * EditarEmpleadoActivity.
 *
 * Ventaja: la pantalla solo observa estados de carga/exito/error y el acceso a
 * Supabase queda detras de EmpleadoRepository.
 */
class RegistroEmpleadoViewModel(private val repository: EmpleadoRepository) : ViewModel() {

    private val _deleteState = MutableLiveData<Resource<Unit>>()
    val deleteState: LiveData<Resource<Unit>> = _deleteState

    private val _updateState = MutableLiveData<Resource<Unit>>()
    val updateState: LiveData<Resource<Unit>> = _updateState

    fun eliminarEmpleado(id: String) {
        // Se agregaron los paréntesis ()
        _deleteState.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = repository.eliminar(id)
                if (response.isSuccessful) {
                    _deleteState.value = Resource.Success(Unit)
                } else {
                    _deleteState.value = Resource.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _deleteState.value = Resource.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }

    fun actualizarEmpleado(id: String, empleado: Empleado) {
        // Se agregaron los paréntesis ()
        _updateState.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = repository.actualizar(id, empleado)
                if (response.isSuccessful) {
                    _updateState.value = Resource.Success(Unit)
                } else {
                    _updateState.value = Resource.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _updateState.value = Resource.Error(e.localizedMessage ?: "Error de red")
            }
        }
    }
}
