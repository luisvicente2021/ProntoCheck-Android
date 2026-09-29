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
import com.example.prontocheck.ui.GestionPersonalActivity
import com.example.prontocheck.ui.gestionUbicaciones.GestionUbicacionesActivity
import com.example.prontocheck.ui.login.LoginActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(AppDependencies.userRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()
        setupObservers()
        setupListeners()
    }

    /**
     * Controla qué opciones puede visualizar el usuario
     * dependiendo de su rol.
     */
    private fun checkPermissions() {

        val prefs = getSharedPreferences(
            "AUTH_PREFS",
            Context.MODE_PRIVATE
        )

        val rol = prefs.getString(
            "USER_ROLE",
            "SEGURIDAD"
        )

        // Por ahora Ubicaciones solamente se muestra al ADMIN.
        if (rol == "ADMIN") {
            binding.cardUbicaciones.visibility = View.VISIBLE
        } else {
            binding.cardUbicaciones.visibility = View.GONE
        }
    }

    /**
     * Observa eventos provenientes del ViewModel.
     */
    private fun setupObservers() {

        viewModel.cerrarSesionEvento.observe(this) { logout ->

            if (logout) {

                val intent = Intent(
                    this,
                    LoginActivity::class.java
                )

                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
                finish()
            }
        }
    }

    /**
     * Configura las opciones disponibles en el Panel de Control.
     *
     * Se mantienen:
     * - Gestión de Empleados
     * - Reloj
     * - Ubicaciones
     * - Cerrar sesión
     */
    private fun setupListeners() {

        // Gestión de Empleados
        binding.btnGestion.setOnClickListener {

            val intent = Intent(
                this,
                GestionPersonalActivity::class.java
            )

            startActivity(intent)
        }

        // Reloj Checador
        binding.btnReloj.setOnClickListener {

            val intent = Intent(
                this,
                RelojActivity::class.java
            )

            startActivity(intent)
        }

        // Gestión de Ubicaciones
        binding.btnUbicaciones.setOnClickListener {

            val intent = Intent(
                this,
                GestionUbicacionesActivity::class.java
            )

            startActivity(intent)
        }

        // Cerrar sesión
        binding.btnCerrarSesion.setOnClickListener {

            val builder =
                androidx.appcompat.app.AlertDialog.Builder(this)

            builder.setTitle("Cerrar Sesión")

            builder.setMessage(
                "¿Estás seguro de que deseas salir?"
            )

            builder.setPositiveButton(
                "Sí, salir"
            ) { _, _ ->

                // Eliminar los datos de la sesión
                getSharedPreferences(
                    "AUTH_PREFS",
                    MODE_PRIVATE
                )
                    .edit()
                    .clear()
                    .apply()

                // Regresar al Login
                val intent = Intent(
                    this,
                    LoginActivity::class.java
                )

                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
                finish()
            }

            builder.setNegativeButton(
                "Cancelar",
                null
            )

            builder.show()
        }
    }
}