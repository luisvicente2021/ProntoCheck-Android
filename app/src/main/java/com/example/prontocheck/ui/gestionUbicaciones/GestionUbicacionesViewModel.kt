package com.example.prontocheck.ui.gestionUbicaciones

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prontocheck.data.model.PuntoAcceso
import com.example.prontocheck.data.model.Residencial
import com.example.prontocheck.utils.Resource
import kotlinx.coroutines.launch

class GestionUbicacionesViewModel(
    private val repository: GestionUbicacionesRepository
) : ViewModel() {

    // PUNTOS DE ACCESO

    private val _puntos =
        MutableLiveData<Resource<List<PuntoAcceso>>>()

    val puntos: LiveData<Resource<List<PuntoAcceso>>> =
        _puntos


    // RESIDENCIALES

    private val _residenciales =
        MutableLiveData<Resource<List<Residencial>>>()

    val residenciales: LiveData<Resource<List<Residencial>>> =
        _residenciales


    // MENSAJES

    private val _mensaje =
        MutableLiveData<String>()

    val mensaje: LiveData<String> =
        _mensaje


    // CARGAR PUNTOS DE ACCESO

    fun cargarPuntos() {

        _puntos.value = Resource.Loading

        viewModelScope.launch {

            try {

                val response =
                    repository.obtenerPuntos()

                if (response.isSuccessful) {

                    _puntos.value =
                        Resource.Success(
                            response.body().orEmpty()
                        )

                } else {

                    val error =
                        response.errorBody()?.string()

                    _puntos.value =
                        Resource.Error(
                            "Error al cargar ubicaciones: " +
                                    (error ?: response.code())
                        )
                }

            } catch (exception: Exception) {

                _puntos.value =
                    Resource.Error(
                        exception.message
                            ?: "Error de conexión al cargar ubicaciones"
                    )
            }
        }
    }


    // CARGAR RESIDENCIALES

    fun cargarResidenciales() {

        _residenciales.value = Resource.Loading

        viewModelScope.launch {

            try {

                val response =
                    repository.obtenerResidenciales()

                if (response.isSuccessful) {

                    _residenciales.value =
                        Resource.Success(
                            response.body().orEmpty()
                        )

                } else {

                    val error =
                        response.errorBody()?.string()

                    _residenciales.value =
                        Resource.Error(
                            "Error al cargar residenciales: " +
                                    (error ?: response.code())
                        )
                }

            } catch (exception: Exception) {

                _residenciales.value =
                    Resource.Error(
                        exception.message
                            ?: "Error de conexión al cargar residenciales"
                    )
            }
        }
    }


    // GUARDAR PUNTO DE ACCESO

    fun guardarPunto(
        residencialId: String,
        nombreResidencial: String,
        nombrePunto: String,
        latitud: Double,
        longitud: Double,
        radioMetros: Double
    ) {

        if (residencialId.isBlank()) {
            _mensaje.value =
                "Selecciona una residencial"
            return
        }

        if (nombrePunto.isBlank()) {
            _mensaje.value =
                "Ingresa el nombre del punto de acceso"
            return
        }

        if (radioMetros <= 0) {
            _mensaje.value =
                "El radio debe ser mayor a 0 metros"
            return
        }

        val nuevoPunto = PuntoAcceso(
            residencialId = residencialId,
            nombreResidencial = nombreResidencial.trim(),
            nombrePunto = nombrePunto.trim(),
            latitud = latitud,
            longitud = longitud,
            radioMetros = radioMetros,
            activo = true
        )

        viewModelScope.launch {

            try {

                val response =
                    repository.guardarPunto(nuevoPunto)

                if (response.isSuccessful) {

                    _mensaje.value =
                        "Punto de acceso guardado"

                    cargarPuntos()

                } else {

                    val error =
                        response.errorBody()?.string()

                    _mensaje.value =
                        "Error al guardar: " +
                                (error ?: response.code())
                }

            } catch (exception: Exception) {

                _mensaje.value =
                    exception.message
                        ?: "Error de conexión al guardar"
            }
        }
    }


    // BAJA LÓGICA DEL PUNTO DE ACCESO

    fun eliminarPunto(
        punto: PuntoAcceso
    ) {

        val id = punto.id

        if (id.isNullOrBlank()) {

            _mensaje.value =
                "No se puede eliminar un punto sin ID"

            return
        }

        viewModelScope.launch {

            try {

                val response =
                    repository.eliminarPunto(id)

                if (response.isSuccessful) {

                    _mensaje.value =
                        "Punto de acceso eliminado"

                    cargarPuntos()

                } else {

                    val error =
                        response.errorBody()?.string()

                    _mensaje.value =
                        "Error al eliminar: " +
                                (error ?: response.code())
                }

            } catch (exception: Exception) {

                _mensaje.value =
                    exception.message
                        ?: "Error de conexión al eliminar"
            }
        }
    }
}