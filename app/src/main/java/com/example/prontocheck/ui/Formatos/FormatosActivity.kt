package com.example.prontocheck.ui.Formatos

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prontocheck.databinding.ActivityFormatosBinding

class FormatosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormatosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Configurar ViewBinding
        binding = ActivityFormatosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Configurar la Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Flecha para regresar al Dashboard
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // 3. Configurar los clics de las tarjetas
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Opción: Bitácora de Acceso
        binding.cardBitacora.setOnClickListener {
            Toast.makeText(this, "Abriendo Bitácora de Acceso...", Toast.LENGTH_SHORT).show()
            // Aquí abrirás la actividad de Bitácora cuando la crees:
            // val intent = Intent(this, BitacoraAccesoActivity::class.java)
            // startActivity(intent)
        }

        // Opción: Reporte de Rondín
        binding.cardRondin.setOnClickListener {
            Toast.makeText(this, "Abriendo Reporte de Rondín...", Toast.LENGTH_SHORT).show()
            // val intent = Intent(this, RondinActivity::class.java)
            // startActivity(intent)
        }

        // Opción: Reporte de Novedades
        binding.cardNovedades.setOnClickListener {
            Toast.makeText(this, "Abriendo Reporte de Novedades...", Toast.LENGTH_SHORT).show()
            // val intent = Intent(this, NovedadesActivity::class.java)
            // startActivity(intent)
        }
    }
}