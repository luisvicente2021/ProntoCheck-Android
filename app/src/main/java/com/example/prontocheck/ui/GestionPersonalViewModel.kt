package com.example.prontocheck.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prontocheck.data.model.Empleado
import com.example.prontocheck.data.repository.EmpleadoRepository
import com.example.prontocheck.utils.Resource
import kotlinx.coroutines.launch

class GestionPersonalViewModel(
    private val repository: EmpleadoRepository
) : ViewModel() {

    private companion object {
        const val PAGE_SIZE = 10
    }

    private val _empleados =
        MutableLiveData<Resource<List<Empleado>>>()

    val empleados: LiveData<Resource<List<Empleado>>> =
        _empleados

    // Resultado de eliminar empleado
    private val _eliminarResultado =
        MutableLiveData<Resource<Unit>>()

    val eliminarResultado: LiveData<Resource<Unit>> =
        _eliminarResultado

    private var paginaActual = 0
    private var estaCargando = false
    private var hayMasDatos = true

    val puedeCargarMas: Boolean
        get() = !estaCargando && hayMasDatos

    fun reiniciar() {
        paginaActual = 0
        hayMasDatos = true
        cargarPagina(reiniciar = true)
    }

    fun cargarSiguientePagina() {
        cargarPagina(reiniciar = false)
    }

    private fun cargarPagina(reiniciar: Boolean) {

        if (estaCargando || !hayMasDatos) return

        estaCargando = true

        if (reiniciar) {
            _empleados.value = Resource.Loading
        }

        viewModelScope.launch {

            try {

                val response = repository.obtenerActivos(
                    limit = PAGE_SIZE,
                    offset = paginaActual * PAGE_SIZE
                )

                android.util.Log.d(
                    "EMPLEADOS_DEBUG",
                    "HTTP=${response.code()} " +
                            "exitoso=${response.isSuccessful} " +
                            "cantidad=${response.body()?.size}"
                )

                if (response.isSuccessful) {

                    val nuevos = response.body().orEmpty()

                    hayMasDatos = nuevos.size >= PAGE_SIZE
                    paginaActual++

                    _empleados.value =
                        Resource.Success(nuevos)

                } else {

                    val error =
                        response.errorBody()?.string()

                    android.util.Log.e(
                        "EMPLEADOS_DEBUG",
                        "HTTP=${response.code()} ERROR=$error"
                    )

                    _empleados.value =
                        Resource.Error(
                            "Error al cargar empleados: ${response.code()}"
                        )
                }

            } catch (exception: Exception) {

                _empleados.value =
                    Resource.Error(
                        exception.message
                            ?: "Error de conexión al cargar empleados"
                    )

            } finally {

                estaCargando = false
            }
        }
    }

    fun eliminarEmpleado(id: String) {

        _eliminarResultado.value =
            Resource.Loading

        viewModelScope.launch {

            try {

                val response =
                    repository.darDeBaja(id)

                if (response.isSuccessful) {

                    _eliminarResultado.value =
                        Resource.Success(Unit)

                } else {

                    val error =
                        response.errorBody()?.string()

                    android.util.Log.e(
                        "EMPLEADOS_DEBUG",
                        "Error eliminando empleado. " +
                                "HTTP=${response.code()} ERROR=$error"
                    )

                    _eliminarResultado.value =
                        Resource.Error(
                            "No se pudo eliminar el empleado"
                        )
                }

            } catch (exception: Exception) {

                android.util.Log.e(
                    "EMPLEADOS_DEBUG",
                    "Excepción eliminando empleado",
                    exception
                )

                _eliminarResultado.value =
                    Resource.Error(
                        exception.message
                            ?: "Error al eliminar empleado"
                    )
            }
        }
    }
}