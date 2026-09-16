package com.example.prontocheck.ui.reporte


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prontocheck.data.model.ResumenEmpleado
import com.example.prontocheck.data.repository.UserRepository
import com.example.prontocheck.domain.usecase.GetReporteUseCase
import com.example.prontocheck.utils.Resource
import kotlinx.coroutines.launch

/**
 * ViewModel de reportes.
 *
 * Por que existe: coordina la obtencion de asistencias y deja a la Activity solo
 * fechas, filtros, lista y exportacion.
 *
 * Ventaja: la regla para generar reportes queda separada y puede crecer hacia
 * UseCases sin tocar la pantalla.
 */
class ReporteViewModel(
    private val repository: UserRepository,
    private val getReporteUseCase: GetReporteUseCase = GetReporteUseCase()
) : ViewModel() {

    private val _reporteState = MutableLiveData<Resource<List<ResumenEmpleado>>>()
    val reporteState: LiveData<Resource<List<ResumenEmpleado>>> = _reporteState

    fun generarReporte(fInicio: String, fFin: String, residencial: String) {
        _reporteState.postValue(Resource.Loading)

        viewModelScope.launch {
            try {
                val response = repository.obtenerReporteAsistencias(fInicio, fFin, residencial)

                if (response.isSuccessful) {
                    val datos = response.body() ?: emptyList()
                    _reporteState.postValue(Resource.Success(datos))
                } else {
                    val errorMsg = "Error: ${response.code()} - Tabla no encontrada"
                    _reporteState.postValue(Resource.Error(errorMsg))
                }
            } catch (e: Exception) {
                _reporteState.postValue(Resource.Error("Fallo de red: ${e.localizedMessage}"))
            }
        }
    }
}
