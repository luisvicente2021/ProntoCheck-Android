package com.example.prontocheck.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.prontocheck.databinding.ActivityEditarEmpleadoBinding

import com.example.prontocheck.di.AppDependencies
import com.example.prontocheck.ui.viewmodel.RegistroEmpleadoViewModel
import com.example.prontocheck.ui.viewmodel.RegistroEmpleadoViewModelFactory
import com.example.prontocheck.utils.Resource

/**
 * Pantalla de edicion de empleado.
 *
 * Por que esta organizada asi: la Activity carga datos del intent, cambia modo
 * edicion y muestra confirmaciones; actualizar/eliminar vive en el ViewModel.
 *
 * Ventaja: se reduce el acoplamiento con Supabase y la UI solo reacciona a
 * estados Resource.
 */
class EditarEmpleadoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarEmpleadoBinding
    private lateinit var viewModel: RegistroEmpleadoViewModel
    private var isEditMode = false
    private var empleadoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        empleadoId = intent.getStringExtra("id")
        cargarDatos()
        setupObservers()

        binding.btnEditarTop.setOnClickListener {
            toggleEditMode()
        }

        binding.btnBorrarTop.setOnClickListener {
            mostrarDialogoEliminar()
        }

        binding.btnGuardarCambios.setOnClickListener {
            ejecutarGuardar()
        }
    }

    private fun setupViewModel() {
        val factory = RegistroEmpleadoViewModelFactory(AppDependencies.empleadoRepository)
        viewModel = ViewModelProvider(this, factory)[RegistroEmpleadoViewModel::class.java]
    }

    private fun cargarDatos() {

        val emailRecibido = intent.getStringExtra("email")

        Log.d("DEBUG_EDIT", "ID: $empleadoId")
        Log.d("DEBUG_EDIT", "EMAIL RECIBIDO: '$emailRecibido'")

        binding.etEditIdEmpleado.setText(empleadoId)
        binding.etEditNombre.setText(intent.getStringExtra("nombre"))
        binding.etEditApePaterno.setText(intent.getStringExtra("apellido_p"))
        binding.etEditApeMaterno.setText(intent.getStringExtra("apellido_m"))
        binding.etEmail.setText(intent.getStringExtra("email"))
        binding.etEditDireccion.setText(intent.getStringExtra("direccion"))
        binding.etEditTelCasa.setText(intent.getStringExtra("tel_casa"))
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode

        val campos = listOf(
            binding.etEditNombre, binding.etEditApePaterno,
            binding.etEditApeMaterno, binding.etEmail,
            binding.etEditDireccion, binding.etEditTelCasa
        )
        campos.forEach { it.isEnabled = isEditMode }

        if (isEditMode) {
            binding.btnEditarTop.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            binding.btnGuardarCambios.visibility = View.VISIBLE
        } else {
            binding.btnEditarTop.setImageResource(android.R.drawable.ic_menu_edit)
            binding.btnGuardarCambios.visibility = View.GONE
            cargarDatos()
        }
    }

    private fun setupObservers() {
        viewModel.deleteState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> { /* Mostrar progreso si quieres */ }
                is Resource.Success -> {
                    Toast.makeText(this, "Eliminado con éxito", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is Resource.Error -> {
                    Toast.makeText(this, "Error al eliminar: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.updateState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnGuardarCambios.isEnabled = false
                    binding.btnGuardarCambios.text = "Guardando..."
                }
                is Resource.Success -> {
                    Toast.makeText(this, "Empleado actualizado correctamente", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is Resource.Error -> {
                    binding.btnGuardarCambios.isEnabled = true
                    binding.btnGuardarCambios.text = "Guardar cambios"
                    Toast.makeText(this, "Error al actualizar: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarDialogoEliminar() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Empleado")
            .setMessage("¿Estás seguro de que deseas eliminar este registro?")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                empleadoId?.let { viewModel.eliminarEmpleado(it) }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun ejecutarGuardar() {
        val id = empleadoId ?: return

        val empleadoEditado = com.example.prontocheck.data.model.Empleado(
            id = id,
            nombre = binding.etEditNombre.text.toString().trim(),
            apellido_paterno = binding.etEditApePaterno.text.toString().trim(),
            apellido_materno = binding.etEditApeMaterno.text.toString().trim(),
            email = binding.etEmail.text.toString().trim(),
            direccion = binding.etEditDireccion.text.toString().trim(),
            telefono_casa = binding.etEditTelCasa.text.toString().trim(),
            residencial = intent.getStringExtra("residencial") ?: ""
        )

        viewModel.actualizarEmpleado(id, empleadoEditado)
    }
}
