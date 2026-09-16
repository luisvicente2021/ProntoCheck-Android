package com.example.prontocheck.ui.AltaEmpleado


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prontocheck.data.model.Empleado
import com.example.prontocheck.data.repository.UserRepository
import com.example.prontocheck.utils.Resource
import kotlinx.coroutines.launch


class AltaEmpleadoViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _registroState = MutableLiveData<Resource<Unit>>()
    val registroState: LiveData<Resource<Unit>> = _registroState

    fun registrarEmpleadoCompleto(empleado: Empleado, passwordTemporal: String) {
        if (empleado.nombre.isNullOrEmpty() || empleado.email.isNullOrEmpty() || passwordTemporal.length < 6) {
            _registroState.value = Resource.Error("Datos incompletos (min. 6 caracteres para password)")
            return
        }

        _registroState.value = Resource.Loading

        viewModelScope.launch {
            try {
                // PASO 1 — Crear usuario en Auth
                // Esto dispara AUTOMÁTICAMENTE el trigger 'on_auth_user_created' en Supabase
                val authResponse = repository.signUp(empleado.email, passwordTemporal)

                if (authResponse.isSuccessful && authResponse.body() != null) {
                    val uuidGenerado = authResponse.body()!!.user?.id

                    if (uuidGenerado != null) {
                        // PASO 2 — ACTUALIZAR (En lugar de insertar)
                        // El trigger ya creó el registro con el ID, ahora llenamos el resto de campos
                        val dbResponse = repository.actualizarEmpleado(uuidGenerado, empleado)

                        if (dbResponse.isSuccessful) {
                            _registroState.value = Resource.Success(Unit)
                        } else {
                            // Si falla la actualización, al menos el usuario ya existe en Auth y DB
                            Log.e("AltaEmpleado", "Error al actualizar detalles: ${dbResponse.errorBody()?.string()}")
                            _registroState.value = Resource.Error("Usuario creado, pero hubo un error al guardar los detalles adicionales.")
                        }
                    } else {
                        _registroState.value = Resource.Error("Error al obtener el ID del nuevo usuario")
                    }
                } else {
                    // Si llegamos aquí, es probable que el email ya exista en auth.users
                    _registroState.value = Resource.Error("Este correo ya está registrado o la contraseña es inválida")
                }
            } catch (e: Exception) {
                Log.e("AltaEmpleado", "Excepción: ${e.message}")
                _registroState.value = Resource.Error(e.message ?: "Error de red inesperado")
            }
        }
    }
}