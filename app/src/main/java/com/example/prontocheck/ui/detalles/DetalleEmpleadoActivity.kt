package com.example.prontocheck.ui.detalles

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.prontocheck.databinding.ActivityDetalleEmpleadoBinding
import com.example.prontocheck.ui.EditarEmpleadoActivity

class DetalleEmpleadoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleEmpleadoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetalleEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarDatos()
        setupListeners()
    }

    private fun cargarDatos() {

        val nombre = intent.getStringExtra("nombre").orEmpty()
        val apellidoP = intent.getStringExtra("apellido_p").orEmpty()
        val apellidoM = intent.getStringExtra("apellido_m").orEmpty()

        val nombreCompleto =
            "$nombre $apellidoP $apellidoM"
                .replace("\\s+".toRegex(), " ")
                .trim()

        binding.tvNombre.text = nombreCompleto

        binding.tvIniciales.text = obtenerIniciales(
            nombre,
            apellidoP
        )

        binding.tvEmail.text =
            intent.getStringExtra("email") ?: "No registrado"

        binding.tvResidencial.text =
            intent.getStringExtra("residencial") ?: "No registrado"

        binding.tvDireccion.text =
            intent.getStringExtra("direccion") ?: "No registrada"

        binding.tvTelefonoCasa.text =
            intent.getStringExtra("tel_casa") ?: "No registrado"

        binding.tvTelefonoEmpresa.text =
            intent.getStringExtra("tel_empresa") ?: "No registrado"

        val jornada =
            intent.getIntExtra("jornada_horas", 8)

        binding.tvJornada.text =
            "$jornada horas"

        binding.tvEstado.text = "ACTIVO"
    }

    private fun setupListeners() {

        binding.btnRegresar.setOnClickListener {
            finish()
        }

        binding.btnEditar.setOnClickListener {

            val intentEditar =
                Intent(
                    this,
                    EditarEmpleadoActivity::class.java
                ).apply {

                    putExtra(
                        "id",
                        intent.getStringExtra("id")
                    )

                    putExtra(
                        "nombre",
                        intent.getStringExtra("nombre")
                    )

                    putExtra(
                        "apellido_p",
                        intent.getStringExtra("apellido_p")
                    )

                    putExtra(
                        "apellido_m",
                        intent.getStringExtra("apellido_m")
                    )

                    putExtra(
                        "email",
                        intent.getStringExtra("email")
                    )

                    putExtra(
                        "residencial",
                        intent.getStringExtra("residencial")
                    )

                    putExtra(
                        "direccion",
                        intent.getStringExtra("direccion")
                    )

                    putExtra(
                        "tel_casa",
                        intent.getStringExtra("tel_casa")
                    )

                    putExtra(
                        "tel_empresa",
                        intent.getStringExtra("tel_empresa")
                    )

                    putExtra(
                        "latitud",
                        intent.getDoubleExtra("latitud", 0.0)
                    )

                    putExtra(
                        "longitud",
                        intent.getDoubleExtra("longitud", 0.0)
                    )
                }

            startActivity(intentEditar)
        }
    }

    private fun obtenerIniciales(
        nombre: String,
        apellido: String
    ): String {

        val primera =
            nombre.firstOrNull()?.uppercaseChar()?.toString().orEmpty()

        val segunda =
            apellido.firstOrNull()?.uppercaseChar()?.toString().orEmpty()

        return primera + segunda
    }
}