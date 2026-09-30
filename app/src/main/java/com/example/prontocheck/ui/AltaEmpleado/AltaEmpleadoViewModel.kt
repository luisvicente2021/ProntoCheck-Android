package com.example.prontocheck.ui.AltaEmpleado

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prontocheck.data.model.Empleado
import com.example.prontocheck.data.repository.EmpleadoRepository
import com.example.prontocheck.utils.Resource
import kotlinx.coroutines.launch

class AltaEmpleadoViewModel(
    private val repository: EmpleadoRepository
) : ViewModel() {

    private val _registroState =
        MutableLiveData<Resource<Unit>>()

    val registroState: LiveData<Resource<Unit>> =
        _registroState

    fun registrarEmpleado(
        empleado: Empleado
    ) {

        // El correo ya NO es obligatorio
        if (
            empleado.nombre.isBlank() ||
            empleado.apellido_paterno.isNullOrBlank()
        ) {

            _registroState.value =
                Resource.Error(
                    "Nombre y apellido paterno son obligatorios"
                )

            return
        }

        _registroState.value =
            Resource.Loading

        viewModelScope.launch {

            try {

                val response =
                    repository.crear(empleado)

                if (response.isSuccessful) {

                    Log.d(
                        "AltaEmpleado",
                        "Empleado registrado correctamente en public.empleados"
                    )

                    _registroState.value =
                        Resource.Success(Unit)

                } else {

                    val error =
                        response.errorBody()?.string()

                    Log.e(
                        "AltaEmpleado",
                        "Error al registrar empleado: $error"
                    )

                    _registroState.value =
                        Resource.Error(
                            error
                                ?: "Error al registrar empleado"
                        )
                }

            } catch (e: Exception) {

                Log.e(
                    "AltaEmpleado",
                    "Excepción al registrar empleado",
                    e
                )

                _registroState.value =
                    Resource.Error(
                        e.message
                            ?: "Error de red inesperado"
                    )
            }
        }
    }
}