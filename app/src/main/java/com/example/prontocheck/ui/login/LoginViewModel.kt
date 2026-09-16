package com.example.prontocheck.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prontocheck.data.model.LoginResponse
import com.example.prontocheck.utils.Resource
import com.example.prontocheck.data.repository.UserRepository
import kotlinx.coroutines.launch



class LoginViewModel(private val repository: UserRepository) : ViewModel() {

    private val _loginState = MutableLiveData<Resource<LoginResponse>>()
    val loginState: LiveData<Resource<LoginResponse>> = _loginState

    fun login(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            _loginState.value = Resource.Error("Ingresa correo y contraseña")
            return
        }

        _loginState.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = repository.login(email, pass)
                if (response.isSuccessful && response.body()?.accessToken != null) {
                    _loginState.value = Resource.Success(response.body()!!)
                } else {
                    // Aquí capturamos el error 400 de credenciales
                    _loginState.value = Resource.Error("Credenciales incorrectas o usuario no encontrado")
                }
            } catch (e: Exception) {
                _loginState.value = Resource.Error(e.message ?: "Error de conexión")
            }
        }
    }
}