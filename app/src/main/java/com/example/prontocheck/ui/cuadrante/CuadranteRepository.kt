package com.example.prontocheck.ui.cuadrante

import com.example.prontocheck.data.repository.EmpleadoRepository

/**
 * Repository del cuadrante.
 *
 * Por que existe: separa la consulta de empleados del editor visual de turnos.
 * Ventaja: cuando exista el endpoint para guardar cuadrantes, se agregara aqui
 * sin ensuciar la tabla dinamica de la Activity.
 */
class CuadranteRepository(private val empleadoRepository: EmpleadoRepository) {

    suspend fun obtenerEmpleados(residencial: String) =
        if (residencial == "Todas") {
            empleadoRepository.obtenerActivos(limit = 100, offset = 0)
        } else {
            empleadoRepository.obtenerPorResidencial(residencial)
        }
}
