package com.example.prontocheck.ui.gestionUbicaciones

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prontocheck.R
import com.example.prontocheck.data.model.PuntoAcceso
import com.example.prontocheck.data.model.Residencial
import com.example.prontocheck.databinding.ActivityGestionUbicacionesBinding
import com.example.prontocheck.di.AppDependencies
import com.example.prontocheck.utils.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class GestionUbicacionesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionUbicacionesBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var adapter: PuntoAccesoAdapter
    private lateinit var viewModel: GestionUbicacionesViewModel

    private var latCapturada: Double? = null
    private var lonCapturada: Double? = null

    private var residenciales: List<Residencial> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGestionUbicacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        setupViewModel()
        setupRecyclerView()
        setupObservers()

        viewModel.cargarPuntos()
        viewModel.cargarResidenciales()

        binding.fabAgregarPunto.setOnClickListener {

            if (residenciales.isEmpty()) {

                Toast.makeText(
                    this,
                    "No hay residenciales disponibles",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                mostrarDialogoAgregar()
            }
        }
    }

    private fun setupViewModel() {

        val factory =
            GestionUbicacionesViewModelFactory(
                AppDependencies.gestionUbicacionesRepository
            )

        viewModel = ViewModelProvider(
            this,
            factory
        )[GestionUbicacionesViewModel::class.java]
    }

    private fun setupObservers() {

        viewModel.puntos.observe(this) { result ->

            when (result) {

                is Resource.Success -> {

                    adapter.actualizarLista(
                        result.data
                    )
                }

                is Resource.Error -> {

                    Toast.makeText(
                        this,
                        result.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Resource.Loading -> Unit
            }
        }

        viewModel.residenciales.observe(this) { result ->

            when (result) {

                is Resource.Success -> {

                    residenciales =
                        result.data
                }

                is Resource.Error -> {

                    Toast.makeText(
                        this,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                Resource.Loading -> Unit
            }
        }

        viewModel.mensaje.observe(this) { mensaje ->

            Toast.makeText(
                this,
                mensaje,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupRecyclerView() {

        adapter =
            PuntoAccesoAdapter(
                emptyList()
            ) { punto ->

                confirmarEliminar(punto)
            }

        binding.rvPuntosAcceso.layoutManager =
            LinearLayoutManager(this)

        binding.rvPuntosAcceso.adapter =
            adapter
    }

    private fun mostrarDialogoAgregar() {

        latCapturada = null
        lonCapturada = null

        val dialogView =
            layoutInflater.inflate(
                R.layout.dialog_agregar_ubicacion,
                null
            )

        val spinnerResidencial =
            dialogView.findViewById<Spinner>(
                R.id.spinnerResidencial
            )

        val etNombrePunto =
            dialogView.findViewById<EditText>(
                R.id.etNombrePunto
            )

        val etRadio =
            dialogView.findViewById<EditText>(
                R.id.etRadioPunto
            )

        val btnGps =
            dialogView.findViewById<Button>(
                R.id.btnCapturarGps
            )

        val tvStatus =
            dialogView.findViewById<TextView>(
                R.id.tvStatusGps
            )

        // Las residenciales ahora vienen de Supabase

        val nombresResidenciales =
            residenciales.map {
                it.nombre
            }

        val spinnerAdapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                nombresResidenciales
            )

        spinnerAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerResidencial.adapter =
            spinnerAdapter

        btnGps.setOnClickListener {

            if (
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->

                        if (location != null) {

                            latCapturada =
                                location.latitude

                            lonCapturada =
                                location.longitude

                            tvStatus.text =
                                "Ubicación capturada: OK\n" +
                                        "Lat: $latCapturada\n" +
                                        "Lon: $lonCapturada"

                            tvStatus.setTextColor(
                                Color.GREEN
                            )

                        } else {

                            Toast.makeText(
                                this,
                                "No se pudo obtener la ubicación. Activa el GPS.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

            } else {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    100
                )
            }
        }

        val dialog =
            AlertDialog.Builder(this)
                .setTitle("Nuevo punto de acceso")
                .setView(dialogView)
                .setPositiveButton(
                    "Guardar",
                    null
                )
                .setNegativeButton(
                    "Cancelar",
                    null
                )
                .create()

        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val posicion =
                    spinnerResidencial.selectedItemPosition

                val residencial =
                    residenciales.getOrNull(
                        posicion
                    )

                val nombrePunto =
                    etNombrePunto.text
                        .toString()
                        .trim()

                val radio =
                    etRadio.text
                        .toString()
                        .toDoubleOrNull()
                        ?: 200.0

                if (residencial == null) {

                    Toast.makeText(
                        this,
                        "Selecciona una residencial",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                if (nombrePunto.isBlank()) {

                    Toast.makeText(
                        this,
                        "Ingresa el nombre del punto de acceso",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                val lat =
                    latCapturada

                val lon =
                    lonCapturada

                if (
                    lat == null ||
                    lon == null
                ) {

                    Toast.makeText(
                        this,
                        "Debes capturar la ubicación GPS",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                viewModel.guardarPunto(
                    residencialId =
                        residencial.id,

                    nombreResidencial =
                        residencial.nombre,

                    nombrePunto =
                        nombrePunto,

                    latitud =
                        lat,

                    longitud =
                        lon,

                    radioMetros =
                        radio
                )

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun confirmarEliminar(
        punto: PuntoAcceso
    ) {

        AlertDialog.Builder(this)
            .setTitle(
                "Eliminar punto de acceso"
            )
            .setMessage(
                "¿Deseas eliminar el punto " +
                        "${punto.nombrePunto} de " +
                        "${punto.nombreResidencial ?: "esta residencial"}?"
            )
            .setPositiveButton(
                "Eliminar"
            ) { _, _ ->

                viewModel.eliminarPunto(
                    punto
                )
            }
            .setNegativeButton(
                "Cancelar",
                null
            )
            .show()
    }
}