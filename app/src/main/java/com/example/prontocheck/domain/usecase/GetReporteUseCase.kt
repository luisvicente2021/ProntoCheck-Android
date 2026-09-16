package com.example.prontocheck.domain.usecase


import com.example.prontocheck.data.model.Asistencia
import com.example.prontocheck.data.model.Empleado
import com.example.prontocheck.ui.reporte.ResumenEmpleado
import java.text.SimpleDateFormat
import java.util.*

class GetReporteUseCase {

    fun execute(
        asistencias: List<Asistencia>,
        empleados: List<Empleado>,
        residencialSeleccionada: String
    ): List<ResumenEmpleado> {

        // 1. Mapa de Residenciales (USANDO VALORES NO NULOS)
        // Usamos filter para ignorar empleados sin ID y !! o elvis para la residencial
        // Usamos associateBy para crear el mapa de forma más segura
        // 1. Mapa de Residenciales (USANDO VALORES NO NULOS)
        val residencialPorId: Map<String, String> = empleados
            .filter { it.id != null } // Filtramos los que no tengan ID
            .associateBy(
                keySelector = { it.id!! },
                valueTransform = { emp ->
                    // Usamos una lógica más explícita para evitar ambigüedades del compilador
                    if (emp.residencial.isNullOrBlank()) "Sin residencial" else emp.residencial!!
                }
            )

        // 2. Filtrar
        val filtradas = if (residencialSeleccionada == "Todas") {
            asistencias
        } else {
            asistencias.filter { residencialPorId[it.idEmpleadoManual] == residencialSeleccionada }
        }

        // 3. Ahora los tipos coinciden perfectamente
        return procesarLogica(filtradas, residencialPorId)
    }

    private fun procesarLogica(
        asistencias: List<Asistencia>,
        residencialPorId: Map<String, String>
    ): List<ResumenEmpleado> {
        val porEmpleado = asistencias.groupBy { it.idEmpleadoManual }
        val resultado = mutableListOf<ResumenEmpleado>()

        for ((idEmpleado, registros) in porEmpleado) {
            val nombre = registros.first().nombreEmpleado
            val residencial = residencialPorId[idEmpleado] ?: "Sin residencial"
            val porFecha = registros.groupBy { it.fecha }
            val detalles = mutableListOf<String>()
            var totalMinutos = 0

            for ((fecha, movimientos) in porFecha.entries.sortedBy { it.key }) {
                val entrada = movimientos.firstOrNull { it.tipo == "entrada" }?.hora ?: "—"
                val salida = movimientos.firstOrNull { it.tipo == "salida" }?.hora ?: "—"

                if (entrada != "—" && salida != "—") {
                    try {
                        val fmtHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        val hEntrada = fmtHora.parse(entrada)
                        val hSalida = fmtHora.parse(salida)
                        if (hSalida != null && hEntrada != null) {
                            totalMinutos += ((hSalida.time - hEntrada.time) / 60000).toInt()
                        }
                    } catch (e: Exception) {}
                }

                try {
                    val d = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha)
                    val fechaFmt = SimpleDateFormat("EEE dd/MM", Locale.getDefault()).format(d!!).uppercase()
                    detalles.add("$fechaFmt   E: $entrada   S: $salida")
                } catch (e: Exception) {
                    detalles.add("$fecha   E: $entrada   S: $salida")
                }
            }

            val horas = totalMinutos / 60
            val mins = totalMinutos % 60
            val totalHoras = if (totalMinutos > 0) "${horas}h ${mins}min" else "Sin cálculo"

            resultado.add(ResumenEmpleado(idEmpleado, nombre, residencial, porFecha.size, detalles, totalHoras))
        }
        return resultado.sortedWith(compareBy({ it.residencial }, { it.nombre }))
    }
}