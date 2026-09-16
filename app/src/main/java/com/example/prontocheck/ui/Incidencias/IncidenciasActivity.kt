package com.example.prontocheck.ui.Incidencias

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prontocheck.data.model.Incidencia
import com.example.prontocheck.databinding.ActivityIncidenciasBinding
import com.example.prontocheck.di.AppDependencies
import com.example.prontocheck.utils.Resource

/**
 * Pantalla de incidencias.
 *
 * Por que se refactorizo: antes esta Activity validaba datos, llamaba Retrofit,
 * transformaba codigos de empleado y actualizaba estado. Ahora solo maneja UI.
 *
 * Ventaja: al seguir MVVM, el flujo queda mas limpio: la Activity pinta y escucha
 * clicks, el ViewModel decide, y el Repository habla con Supabase.
 */
class IncidenciasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncidenciasBinding
    private lateinit var adapter: IncidenciasAdapter
    private lateinit var viewModel: IncidenciasViewModel
    private var puedeGestionarIncidencias = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncidenciasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        setupObservers()

        val prefs = getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE)
        val rol = prefs.getString("USER_ROLE", "SEGURIDAD")
        puedeGestionarIncidencias = rol == "RH" || rol == "ADMIN"

        setupBotonRegistro()

        if (puedeGestionarIncidencias) {
            binding.layoutListaRH.visibility = View.VISIBLE
            binding.cardRegistrarIncidencia.visibility = View.VISIBLE
            viewModel.cargarIncidencias()
        } else {
            binding.layoutListaRH.visibility = View.GONE
            binding.cardRegistrarIncidencia.visibility = View.VISIBLE
        }

        binding.btnExportarPDF.setOnClickListener {
            Toast.makeText(this, "Generando reporte de incidencias...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViewModel() {
        val factory = IncidenciasViewModelFactory(AppDependencies.incidenciasRepository)
        viewModel = ViewModelProvider(this, factory)[IncidenciasViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.incidencias.observe(this) { result ->
            when (result) {
                is Resource.Success -> adapter.actualizarLista(result.data)
                is Resource.Error -> Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                Resource.Loading -> Unit
            }
        }

        viewModel.mensaje.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            if (mensaje == "Solicitud enviada correctamente") {
                binding.etEmpleadoId.text?.clear()
                binding.etMotivo.text?.clear()
            }
        }
    }

    private fun setupBotonRegistro() {
        binding.btnEnviarIncidencia.setOnClickListener {
            viewModel.registrarIncidencia(
                codigoEmpleado = binding.etEmpleadoId.text.toString().trim(),
                tipo = binding.spinnerTipo.selectedItem.toString(),
                motivo = binding.etMotivo.text.toString().trim(),
                puedeGestionar = puedeGestionarIncidencias
            )
        }
    }

    private fun setupRecyclerView() {
        adapter = IncidenciasAdapter(emptyList()) { incidencia ->
            if (incidencia.estado == "Pendiente") {
                mostrarDialogoGestion(incidencia)
            } else {
                Toast.makeText(this, "Esta incidencia ya esta ${incidencia.estado}", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvIncidencias.layoutManager = LinearLayoutManager(this)
        binding.rvIncidencias.adapter = adapter
    }

    private fun mostrarDialogoGestion(incidencia: Incidencia) {
        AlertDialog.Builder(this)
            .setTitle("Gestionar Incidencia")
            .setMessage("Empleado: ${incidencia.nombre_empleado ?: "ID: " + incidencia.empleado_id}\nMotivo: ${incidencia.motivo}")
            .setPositiveButton("Justificar") { _, _ ->
                viewModel.actualizarEstado(incidencia, "Justificada")
            }
            .setNegativeButton("No Justificar") { _, _ ->
                viewModel.actualizarEstado(incidencia, "No Justificada")
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }
}
