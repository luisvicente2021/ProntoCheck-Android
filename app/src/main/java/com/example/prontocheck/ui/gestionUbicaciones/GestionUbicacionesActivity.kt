package com.example.prontocheck.ui.gestionUbicaciones

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prontocheck.R
import com.example.prontocheck.data.model.PuntoAcceso
import com.example.prontocheck.databinding.ActivityGestionUbicacionesBinding
import com.example.prontocheck.di.AppDependencies
import com.example.prontocheck.utils.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * Pantalla de gestion de ubicaciones.
 *
 * Por que se refactorizo: antes mezclaba permisos, GPS, dialogs y llamadas de
 * red en una sola clase. Ahora la Activity conserva lo propio de Android UI/GPS
 * y delega las operaciones de datos al ViewModel.
 *
 * Ventaja: el flujo queda mas mantenible y cada cambio tiene un lugar claro:
 * UI aqui, estado en ViewModel, Supabase en Repository.
 */
class GestionUbicacionesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionUbicacionesBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var adapter: PuntoAccesoAdapter
    private lateinit var viewModel: GestionUbicacionesViewModel
    private var latCapturada: Double = 0.0
    private var lonCapturada: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionUbicacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupViewModel()
        setupRecyclerView()
        setupObservers()
        viewModel.cargarPuntos()

        binding.fabAgregarPunto.setOnClickListener {
            mostrarDialogoAgregar()
        }
    }

    private fun setupViewModel() {
        val factory = GestionUbicacionesViewModelFactory(AppDependencies.gestionUbicacionesRepository)
        viewModel = ViewModelProvider(this, factory)[GestionUbicacionesViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.puntos.observe(this) { result ->
            when (result) {
                is Resource.Success -> adapter.actualizarLista(result.data)
                is Resource.Error -> Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                Resource.Loading -> Unit
            }
        }

        viewModel.mensaje.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = PuntoAccesoAdapter(emptyList()) { punto ->
            confirmarEliminar(punto)
        }
        binding.rvPuntosAcceso.layoutManager = LinearLayoutManager(this)
        binding.rvPuntosAcceso.adapter = adapter
    }

    private fun mostrarDialogoAgregar() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_ubicacion, null)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombrePunto)
        val etRadio = dialogView.findViewById<EditText>(R.id.etRadioPunto)
        val btnGps = dialogView.findViewById<Button>(R.id.btnCapturarGps)
        val tvStatus = dialogView.findViewById<TextView>(R.id.tvStatusGps)

        btnGps.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        latCapturada = location.latitude
                        lonCapturada = location.longitude
                        tvStatus.text = "Ubicacion capturada: OK\nLat: $latCapturada\nLon: $lonCapturada"
                        tvStatus.setTextColor(Color.GREEN)
                    } else {
                        Toast.makeText(this, "No se pudo obtener la ubicacion. Activa el GPS.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Nueva Residencial")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val radio = etRadio.text.toString().toDoubleOrNull() ?: 100.0
                viewModel.guardarPunto(
                    nombre = etNombre.text.toString(),
                    latitud = latCapturada,
                    longitud = lonCapturada,
                    radioMetros = radio
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarEliminar(punto: PuntoAcceso) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Ubicacion")
            .setMessage("Deseas eliminar la residencial ${punto.nombre_residencial}?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarPunto(punto)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
