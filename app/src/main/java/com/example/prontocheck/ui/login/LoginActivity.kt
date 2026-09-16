package com.example.prontocheck.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.prontocheck.ui.dashboard.DashboardActivity
import com.example.prontocheck.utils.Resource
import com.example.prontocheck.databinding.ActivityMainBinding
import com.example.prontocheck.di.AppDependencies

// Importamos RelojActivity por si el usuario es de SEGURIDAD
import com.example.prontocheck.RelojActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(AppDependencies.userRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            if (binding.progressBar.visibility == View.VISIBLE) return@setOnClickListener
            validarYEntrar()
        }
    }

    private fun validarYEntrar() {
        binding.txtEmail.error = null
        binding.txtPassword.error = null

        // Sugerencia: Cambiar a binding.txtEmail.text... cuando termines tus pruebas
        val email = binding.txtEmail.text.toString().trim().ifEmpty { "luis@prontocheck.com" }
        val password = binding.txtPassword.text.toString().trim().ifEmpty { "123456" }

        var isValid = true

        if (email.isEmpty()) {
            binding.txtEmail.error = "El correo es obligatorio"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.txtEmail.error = "Formato de correo inválido"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.txtPassword.error = "La contraseña es obligatoria"
            isValid = false
        } else if (password.length < 6) {
            binding.txtPassword.error = "Mínimo 6 caracteres"
            isValid = false
        }

        if (isValid) {
            ocultarTeclado()
            viewModel.login(email, password)
        }
    }

    private fun setupObservers() {
        viewModel.loginState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> toggleLoading(true)
                is Resource.Success -> {
                    toggleLoading(false)

                    // --- LÓGICA DE ROLES AQUÍ ---
                    val respuesta = resource.data
                    val token = respuesta?.accessToken

                    // Extraemos el rol del objeto metadata que definimos en LoginResponse
                    val rol = respuesta?.user?.metadata?.rol ?: "SEGURIDAD"
                    val nombre = respuesta?.user?.email ?: "Usuario"

                    // Guardamos en SharedPreferences para que toda la app lo sepa
                    guardarSesion(token, rol, nombre)

                    // Navegamos según el rol
                    irAPantallaSegunRol(rol)
                }
                is Resource.Error -> {
                    toggleLoading(false)
                    val errorMsg = when {
                        resource.message?.contains("400") == true -> "Correo o contraseña incorrectos"
                        resource.message?.contains("Unable to resolve host") == true -> "Sin conexión a internet"
                        else -> resource.message ?: "Ocurrió un error inesperado"
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun guardarSesion(token: String?, rol: String, email: String) {
        val prefs = getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("TOKEN", token)
            putString("USER_ROLE", rol)
            putString("USER_EMAIL", email)
            apply()
        }
    }

    private fun irAPantallaSegunRol(rol: String) {
        val intent = when (rol) {
            "SEGURIDAD" -> {
                // El guardia va directo al reloj checador
                Intent(this, RelojActivity::class.java)
            }
            "ADMIN", "RECURSOS_HUMANOS" -> {
                // Los jefes van al Dashboard para gestionar cosas
                Intent(this, DashboardActivity::class.java)
            }
            else -> Intent(this, DashboardActivity::class.java)
        }

        intent.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun toggleLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }

    private fun ocultarTeclado() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
