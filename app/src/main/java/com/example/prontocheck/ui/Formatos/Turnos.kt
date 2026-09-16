package com.example.prontocheck.ui.Formatos


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prontocheck.databinding.ActivityTurnosBinding
import com.example.prontocheck.ui.cuadrante.CuadranteActivity

class TurnosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTurnosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTurnosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Formatos de Turno"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // --- EVENTOS DE CLIC ---

        binding.cardBitacora.setOnClickListener {
            Toast.makeText(this, "Abriendo Bitácora...", Toast.LENGTH_SHORT).show()
        }

        binding.cardRondin.setOnClickListener {
            // Si ya tienes RondinActivity, puedes descomentar la navegación:
            // val intent = Intent(this, RondinActivity::class.java)
            // startActivity(intent)
            Toast.makeText(this, "Abriendo Reporte de Rondín...", Toast.LENGTH_SHORT).show()
        }

        binding.cardNovedades.setOnClickListener {
            Toast.makeText(this, "Abriendo Reporte de Novedades...", Toast.LENGTH_SHORT).show()
        }

        // BOTÓN DE CUADRANTE (ROL DE TURNOS)
        binding.cardCuadrante.setOnClickListener {
            // Este Toast te confirmará si el botón funciona
            Toast.makeText(this, "Navegando a Gestión de Cuadrante...", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, CuadranteActivity::class.java)
            startActivity(intent)
        }
    } // Cierre de onCreate
} // Cierre de la clase