package com.example.prontocheck.ui.cuadrante

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prontocheck.data.model.Empleado
import com.example.prontocheck.utils.Resource
import kotlinx.coroutines.launch

/**
 * ViewModel del cuadrante.
 *
 * Por que existe: carga empleados por residencial y deja a la Activity solo la
 * responsabilidad de construir la tabla interactiva.
 *
 * Ventaja: el filtro de residencial y los errores de red quedan en un flujo
 * observable, facil de mantener y de probar.
 */
class CuadranteViewModel(
    private val repository: CuadranteRepository
) : ViewModel() {

    private val _empleados = MutableLiveData<Resource<List<Empleado>>>()
    val empleados: LiveData<Resource<List<Empleado>>> = _empleados

    fun cargarEmpleados(residencial: String) {
        _empleados.value = Resource.Loading

        viewModelScope.launch {
            try {
                val response = repository.obtenerEmpleados(residencial)
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
}
