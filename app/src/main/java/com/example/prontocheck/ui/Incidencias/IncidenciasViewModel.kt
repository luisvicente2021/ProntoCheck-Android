package com.example.prontocheck.ui.Incidencias

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prontocheck.data.model.Incidencia
import com.example.prontocheck.utils.Resource
import kotlinx.coroutines.launch

/**
 * ViewModel de incidencias.
 *
 * Por que existe: mantiene la logica de negocio fuera del Activity. Aqui se
 * valida, se decide cuando recargar y se transforma el codigo de empleado al UUID
 * que necesita Supabase.
 *
 * Ventaja: la pantalla queda enfocada en pintar datos y reaccionar a clicks; esta
 * clase se puede probar sin Android UI y sobrevive a cambios de configuracion.
 */
class IncidenciasViewModel(
    private val repository: IncidenciasRepository
) : ViewModel() {

    private val _incidencias = MutableLiveData<Resource<List<Incidencia>>>()
    val incidencias: LiveData<Resource<List<Incidencia>>> = _incidencias

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    fun cargarIncidencias() {
        _incidencias.value = Resource.Loading

        viewModelScope.launch {
            try {
                val response = repository.obtenerIncidencias()
                if (response.isSuccessful) {
                    _incidencias.value = Resource.Success(response.body().orEmpty())
                } else {
                    _incidencias.value = Resource.Error("Error al cargar incidencias")
                }
            } catch (exception: Exception) {
                _incidencias.value = Resource.Error(
                    exception.message ?: "Error de conexion al cargar incidencias"
                )
            }
        }
    }

    fun registrarIncidencia(codigoEmpleado: String, tipo: String, motivo: String, puedeGestionar: Boolean) {
        if (codigoEmpleado.isBlank() || motivo.isBlank()) {
            _mensaje.value = "Por favor, completa todos los campos"
            return
        }

        viewModelScope.launch {
            try {
                val empleadoResponse = repository.buscarEmpleadoPorCodigo(codigoEmpleado.trim())
                val empleado = empleadoResponse.body()?.firstOrNull()

                if (!empleadoResponse.isSuccessful || empleado == null) {
                    _mensaje.value = "Error: El codigo de empleado no existe"
                    return@launch
                }

                val incidencia = Incidencia(
                    empleado_id = empleado.id.toString(),
                    tipo = tipo,
                    motivo = motivo.trim(),
                    estado = "Pendiente"
                )

                val response = repository.registrarIncidencia(incidencia)
                if (response.isSuccessful) {
                    _mensaje.value = "Solicitud enviada correctamente"
                    if (puedeGestionar) cargarIncidencias()
                } else {
                    _mensaje.value = "Error al registrar: ${response.message()}"
                }
            } catch (exception: Exception) {
                _mensaje.value = exception.message ?: "Error de red al registrar incidencia"
            }
        }
    }

    fun actualizarEstado(incidencia: Incidencia, nuevoEstado: String) {
        val id = incidencia.id
        if (id == null) {
            _mensaje.value = "No se puede actualizar una incidencia sin ID"
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.actualizarEstado(id, nuevoEstado)
                if (response.isSuccessful) {
                    _mensaje.value = "Estado actualizado: $nuevoEstado"
                    cargarIncidencias()
                } else {
                    _mensaje.value = "Error al actualizar"
                }
            } catch (exception: Exception) {
                _mensaje.value = exception.message ?: "Error de conexion al actualizar"
            }
        }
    }
}
