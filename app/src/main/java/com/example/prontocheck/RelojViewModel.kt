package com.example.prontocheck

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prontocheck.data.model.Asistencia
import com.example.prontocheck.data.model.Empleado
import com.example.prontocheck.data.model.PuntoAcceso
import com.example.prontocheck.utils.Resource
import kotlinx.coroutines.launch

/**
 * ViewModel del reloj checador.
 *
 * Por que existe: maneja datos remotos del reloj sin mezclar Retrofit con la
 * logica de camara, GPS y FaceNet.
 *
 * Ventaja: RelojActivity queda enfocada en sensores e interaccion; esta clase
 * expone puntos, empleados y resultado del registro como estado observable.
 */
class RelojViewModel(private val repository: RelojRepository) : ViewModel() {

    private val _puntosAcceso = MutableLiveData<Resource<List<PuntoAcceso>>>()
    val puntosAcceso: LiveData<Resource<List<PuntoAcceso>>> = _puntosAcceso

    private val _empleados = MutableLiveData<Resource<List<Empleado>>>()
    val empleados: LiveData<Resource<List<Empleado>>> = _empleados

    private val _registro = MutableLiveData<Resource<Unit>>()
    val registro: LiveData<Resource<Unit>> = _registro

    private val _siguienteMovimiento = MutableLiveData<Resource<String>>()
    val siguienteMovimiento: LiveData<Resource<String>> = _siguienteMovimiento

    fun cargarPuntosAcceso() {
        _puntosAcceso.value = Resource.Loading

        viewModelScope.launch {
            try {
                val response = repository.obtenerPuntosAcceso()
                if (response.isSuccessful) {
                    _puntosAcceso.value = Resource.Success(response.body().orEmpty())
                } else {
                    _puntosAcceso.value = Resource.Error("Error al cargar puntos de acceso")
                }
            } catch (exception: Exception) {
                _puntosAcceso.value = Resource.Error(
                    exception.message ?: "Error de conexion al cargar puntos"
                )
            }
        }
    }

    fun cargarEmpleadosConRostro() {
        _empleados.value = Resource.Loading

        viewModelScope.launch {
            try {
                val response = repository.obtenerEmpleadosConRostro()
                if (response.isSuccessful) {
                    _empleados.value = Resource.Success(response.body().orEmpty())
                } else {
                    _empleados.value = Resource.Error("Error al cargar empleados")
                }
            } catch (exception: Exception) {
                _empleados.value = Resource.Error(
                    exception.message ?: "Error de conexion al cargar empleados"
                )
            }
        }
    }

    fun registrarAsistencia(asistencia: Asistencia) {
        _registro.value = Resource.Loading

        viewModelScope.launch {
            try {
                val response = repository.registrarAsistencia(asistencia)
                if (response.isSuccessful || response.code() == 201) {
                    _registro.value = Resource.Success(Unit)
                } else {
                    _registro.value = Resource.Error("Error al registrar: ${response.code()}")
                }
            } catch (exception: Exception) {
                _registro.value = Resource.Error(
                    exception.message ?: "Error de conexion al registrar"
                )
            }
        }
    }

    fun obtenerSiguienteMovimiento(empleadoId: String) {
        _siguienteMovimiento.value = Resource.Loading

        viewModelScope.launch {
            try {
                val response = repository.obtenerUltimaAsistencia(empleadoId)

                if (response.isSuccessful) {
                    val ultima = response.body()?.firstOrNull()

                    if (ultima == null) {
                        // Nunca ha registrado asistencia
                        _siguienteMovimiento.value =
                            Resource.Success("entrada")
                        return@launch
                    }

                    val fechaUltimoRegistro = try {
                        java.time.OffsetDateTime
                            .parse(ultima.fecha_hora)
                            .atZoneSameInstant(
                                java.time.ZoneId.systemDefault()
                            )
                            .toLocalDate()
                    } catch (e: Exception) {
                        null
                    }

                    val hoy = java.time.LocalDate.now()

                    val siguiente = if (fechaUltimoRegistro != hoy) {

                        // El último registro pertenece a otro día.
                        // Una nueva jornada comienza con ENTRADA.
                        "entrada"

                    } else {

                        // El registro pertenece al día actual.
                        when (ultima.tipo.lowercase()) {
                            "entrada" -> "salida"
                            "salida" -> "entrada"
                            else -> "entrada"
                        }
                    }

                    _siguienteMovimiento.value =
                        Resource.Success(siguiente)

                } else {
                    _siguienteMovimiento.value =
                        Resource.Error(
                            "No se pudo consultar la última asistencia"
                        )
                }

            } catch (exception: Exception) {
                _siguienteMovimiento.value =
                    Resource.Error(
                        exception.message
                            ?: "Error de conexión al consultar la última asistencia"
                    )
            }
        }
    }
}
