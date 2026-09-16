package com.example.prontocheck.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.prontocheck.RelojActivity
import com.example.prontocheck.databinding.ActivityDashboardBinding
import com.example.prontocheck.di.AppDependencies
import com.example.prontocheck.ui.Formatos.TurnosActivity
import com.example.prontocheck.ui.GestionPersonalActivity
import com.example.prontocheck.ui.Incidencias.IncidenciasActivity
import com.example.prontocheck.ui.gestionUbicaciones.GestionUbicacionesActivity
import com.example.prontocheck.ui.login.LoginActivity
import com.example.prontocheck.ui.reporte.ReporteActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(AppDependencies.userRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dentro del onCreate de DashboardActivity

        // 1. Verificar el rol del usuario para mostrar/ocultar módulos administrativos
        checkPermissions()

        // 2. Configurar componentes
        setupObservers()
        setupListeners()

    }

    private fun checkPermissions() {
        // Recuperamos el rol guardado en SharedPreferences durante el Login
        val prefs = getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE)
        val rol = prefs.getString("USER_ROLE", "SEGURIDAD")

        // Si el usuario es ADMIN, habilitamos la sección de Geocercas
        if (rol == "ADMIN") {
            //binding.labelConfiguracion.visibility = View.VISIBLE
            binding.cardUbicaciones.visibility = View.VISIBLE
        } else {
            // Para cualquier otro rol, nos aseguramos de que esté oculto
           // binding.labelConfiguracion.visibility = View.GONE
            binding.cardUbicaciones.visibility = View.GONE
        }
    }

    private fun setupObservers() {
        // Observar evento de cerrar sesión
        viewModel.cerrarSesionEvento.observe(this) { logout ->
            if (logout) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun setupListeners() {
        // Botón Reloj Checador
        binding.btnReloj.setOnClickListener {
            startActivity(Intent(this, RelojActivity::class.java))
        }

        // Botón Reportes
        binding.btnIncidencias.setOnClickListener {
            startActivity(Intent(this, ReporteActivity::class.java))
        }

        // Botón Incidencias
        binding.btnIncidencias.setOnClickListener {
            startActivity(Intent(this, IncidenciasActivity::class.java))
        }

        // Botón Gestión de Personal (CRUD Empleados)
        binding.btnGestion.setOnClickListener {
            startActivity(Intent(this, GestionPersonalActivity::class.java))
        }

        // Botón Gestión de Ubicaciones (Geocercas) - Solo visible para ADMIN
        binding.btnUbicaciones.setOnClickListener {
            startActivity(Intent(this, GestionUbicacionesActivity::class.java))
        }

        binding.btnCerrarSesion.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Cerrar Sesión")
            builder.setMessage("¿Estás seguro de que deseas salir?")
            builder.setPositiveButton("Sí, salir") { _, _ ->
                // Limpia los datos
                getSharedPreferences("AUTH_PREFS", MODE_PRIVATE).edit().clear().apply()
                // Ir al Login
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            builder.setNegativeButton("Cancelar", null)
            builder.show()
        }

        // Dentro del onCreate de DashboardActivity.kt
        binding.btnFormatos.setOnClickListener {
            // Intent para ir de la pantalla actual a TurnosActivity
            val intent = Intent(this, TurnosActivity::class.java)
            startActivity(intent)
        }

        // Dentro de DashboardActivity
        binding.btnInventario.setOnClickListener {
            val intent = Intent(this, com.example.prontocheck.ui.productos.InventarioActivity::class.java)
            startActivity(intent)
        }
    }
}
