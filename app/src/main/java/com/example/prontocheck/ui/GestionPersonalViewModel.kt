package com.example.prontocheck.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prontocheck.data.model.Empleado
import com.example.prontocheck.data.repository.EmpleadoRepository
import com.example.prontocheck.utils.Resource
import kotlinx.coroutines.launch

/**
 * ViewModel de GestionPersonalActivity.
 *
 * Por que existe: maneja paginacion y carga de empleados fuera de la pantalla.
 * La Activity ya no necesita conocer offsets, limites ni llamadas a Supabase.
 *
 * Ventaja: la pantalla se enfoca en RecyclerView, navegacion y estados visuales,
 * mientras esta clase conserva el estado de pagina y evita cargas duplicadas.
 */
class GestionPersonalViewModel(
    private val repository: EmpleadoRepository
) : ViewModel() {

    private companion object {
        const val PAGE_SIZE = 10
    }

    private val _empleados = MutableLiveData<Resource<List<Empleado>>>()
    val empleados: LiveData<Resource<List<Empleado>>> = _empleados

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
        if (reiniciar) _empleados.value = Resource.Loading

        viewModelScope.launch {
            try {
                val response = repository.obtenerActivos(
                    limit = PAGE_SIZE,
                    offset = paginaActual * PAGE_SIZE
                )

                if (response.isSuccessful) {
                    val nuevos = response.body().orEmpty()
                    hayMasDatos = nuevos.size >= PAGE_SIZE
                    paginaActual++
                    _empleados.value = Resource.Success(nuevos)
                } else {
                    _empleados.value = Resource.Error("Error al cargar empleados: ${response.code()}")
                }
            } catch (exception: Exception) {
                _empleados.value = Resource.Error(
                    exception.message ?: "Error de conexion al cargar empleados"
                )
            } finally {
                estaCargando = false
            }
        }
    }
}
