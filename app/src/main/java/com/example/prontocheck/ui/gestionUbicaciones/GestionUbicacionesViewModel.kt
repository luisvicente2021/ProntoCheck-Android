package com.example.prontocheck.ui.gestionUbicaciones

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prontocheck.data.model.PuntoAcceso
import com.example.prontocheck.utils.Resource
import kotlinx.coroutines.launch

/**
 * ViewModel de gestion de ubicaciones.
 *
 * Por que existe: concentra la logica de cargar, crear y eliminar puntos de
 * acceso. El Activity solo se queda con permisos, dialogos y render de pantalla.
 *
 * Ventaja: reduce duplicacion, hace la pantalla mas pequena y mantiene el estado
 * observable en LiveData para que la UI se actualice de forma consistente.
 */
class GestionUbicacionesViewModel(
    private val repository: GestionUbicacionesRepository
) : ViewModel() {

    private val _puntos = MutableLiveData<Resource<List<PuntoAcceso>>>()
    val puntos: LiveData<Resource<List<PuntoAcceso>>> = _puntos

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    fun cargarPuntos() {
        _puntos.value = Resource.Loading

        viewModelScope.launch {
            try {
                val response = repository.obtenerPuntos()
                if (response.isSuccessful) {
                    _puntos.value = Resource.Success(response.body().orEmpty())
                } else {
                    _puntos.value = Resource.Error("Error al cargar ubicaciones")
                }
            } catch (exception: Exception) {
                _puntos.value = Resource.Error(
                    exception.message ?: "Error de conexion al cargar ubicaciones"
                )
            }
        }
    }

    fun guardarPunto(nombre: String, latitud: Double, longitud: Double, radioMetros: Double) {
        if (nombre.isBlank() || latitud == 0.0) {
            _mensaje.value = "Faltan datos o capturar GPS"
            return
        }

        val nuevoPunto = PuntoAcceso(
            nombre_residencial = nombre.trim(),
            residencial_id = null,
            latitud = latitud,
            longitud = longitud,
            radio_metros = radioMetros,
            activo = true
        )

        viewModelScope.launch {
            try {
                val response = repository.guardarPunto(nuevoPunto)
                if (response.isSuccessful) {
                    _mensaje.value = "Residencial guardada"
                    cargarPuntos()
                } else {
                    _mensaje.value = "Error al guardar"
                }
            } catch (exception: Exception) {
                _mensaje.value = exception.message ?: "Error de conexion al guardar"
            }
        }
    }

    fun eliminarPunto(punto: PuntoAcceso) {
        val id = punto.id
        if (id.isNullOrBlank()) {
            _mensaje.value = "No se puede eliminar un punto sin ID"
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.eliminarPunto(id)
                if (response.isSuccessful) {
                    _mensaje.value = "Punto eliminado"
                    cargarPuntos()
                } else {
                    _mensaje.value = "Error al eliminar"
                }
            } catch (exception: Exception) {
                _mensaje.value = exception.message ?: "Error de conexion al eliminar"
            }
        }
    }
}
