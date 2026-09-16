package com.example.prontocheck.ui.reporte

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prontocheck.data.model.ResumenEmpleado
import com.example.prontocheck.databinding.ActivityReporteBinding
import com.example.prontocheck.di.AppDependencies
import com.example.prontocheck.utils.Resource
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ReporteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReporteBinding
    private lateinit var adapter: ReporteAdapter

    private val viewModel: ReporteViewModel by viewModels {
        ReporteViewModelFactory(AppDependencies.userRepository)
    }

    private var fechaInicio: Calendar? = null
    private var fechaFin: Calendar? = null
    private val fmtMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val fmtApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val residenciales = listOf(
        "Todas", "Altai", "Aqua", "Arbolada", "Cumbres", "Palmaris", "Rio", "Via Cumbres"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReporteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupSpinner() {
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            residenciales
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerResidencial.adapter = spinnerAdapter
    }

    private fun setupRecyclerView() {
        adapter = ReporteAdapter()
        binding.recyclerReporte.layoutManager = LinearLayoutManager(this)
        binding.recyclerReporte.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnSeleccionarInicio.setOnClickListener { mostrarDatePicker(esInicio = true) }
        binding.btnSeleccionarFin.setOnClickListener { mostrarDatePicker(esInicio = false) }

        binding.btnGenerarReporte.setOnClickListener {
            prepararGeneracionReporte()
        }

        binding.btnExportarPDF.setOnClickListener {
            exportarCSV()
        }
    }

    private fun observeViewModel() {
        viewModel.reporteState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> mostrarEstado("cargando")
                is Resource.Success<*> -> {
                    val reporte = (resource as Resource.Success<List<ResumenEmpleado>>).data
                    if (reporte.isEmpty()) {
                        binding.tvMensajeVacio.text = "Sin asistencias para este período"
                        mostrarEstado("vacio")
                    } else {
                        adapter.submitList(reporte)
                        mostrarEstado("lista")
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                    mostrarEstado("vacio")
                }
            }
        }
    }

    private fun mostrarDatePicker(esInicio: Boolean) {
        val hoy = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val cal = Calendar.getInstance().apply { set(year, month, day) }
            if (esInicio) {
                fechaInicio = cal
                binding.tvFechaInicio.text = fmtMostrar.format(cal.time)
            } else {
                fechaFin = cal
                binding.tvFechaFin.text = fmtMostrar.format(cal.time)
            }
        }, hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun prepararGeneracionReporte() {
        val inicio = fechaInicio
        val fin = fechaFin

        if (inicio == null || fin == null) {
            Toast.makeText(this, "Selecciona fecha de inicio y fin", Toast.LENGTH_SHORT).show()
            return
        }

        val residencial = binding.spinnerResidencial.selectedItem.toString()

        // LLAMADA CORRECTA AL VIEWMODEL
        viewModel.generarReporte(
            fInicio = fmtApi.format(inicio.time),
            fFin = fmtApi.format(fin.time),
            residencial = residencial
        )
    }

    private fun exportarCSV() {
        val resource = viewModel.reporteState.value
        if (resource !is Resource.Success) {
            Toast.makeText(this, "Primero genera un reporte con datos", Toast.LENGTH_SHORT).show()
            return
        }

        val reporte = resource.data
        val csvString = StringBuilder()

        // Encabezados
        csvString.append("Empleado,Residencial,Dias Asistidos,Horas Totales,Horas Normales (8h),Horas Extras\n")

        for (emp in reporte) {
            val totalHorasNum = emp.totalHoras.toDoubleOrNull() ?: 0.0
            val jornadaBaseTotal = emp.diasAsistidos * 8.0

            val horasNormales = if (totalHorasNum >= jornadaBaseTotal) jornadaBaseTotal else totalHorasNum
            val horasExtras = if (totalHorasNum > jornadaBaseTotal) totalHorasNum - jornadaBaseTotal else 0.0

            csvString.append("${emp.nombre},${emp.residencial},${emp.diasAsistidos},${totalHorasNum},${horasNormales},${horasExtras}\n")
        }

        guardarArchivoCSV(csvString.toString())
    }

    private fun guardarArchivoCSV(contenido: String) {
        try {
            val fecha = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Reporte_$fecha.csv")
            file.writeText(contenido)

            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Compartir Reporte Excel"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarEstado(estado: String) {
        binding.layoutCargando.visibility = if (estado == "cargando") View.VISIBLE else View.GONE
        binding.layoutVacio.visibility    = if (estado == "vacio")    View.VISIBLE else View.GONE
        binding.recyclerReporte.visibility = if (estado == "lista")   View.VISIBLE else View.GONE
        binding.layoutResumen.visibility  = if (estado == "lista")    View.VISIBLE else View.GONE
    }
}
