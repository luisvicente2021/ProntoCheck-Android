package com.example.prontocheck.ui.productos


import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prontocheck.R
import com.example.prontocheck.databinding.ActivityInventarioBinding
import com.example.prontocheck.di.AppDependencies


/**
 * Pantalla de inventario.
 *
 * Por que esta organizada asi: la Activity solo muestra la lista, abre el dialogo
 * de alta y envia eventos al ViewModel.
 *
 * Ventaja: stock, guardado y errores viven fuera de la UI, por lo que el modulo
 * se mantiene consistente con MVVM.
 */
class InventarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventarioBinding
    private lateinit var adapter: InventarioAdapter

    private val viewModel: InventarioViewModel by viewModels {
        InventarioViewModelFactory(AppDependencies.inventarioRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        viewModel.cargarInventario()
    }

    private fun setupUI() {
        adapter = InventarioAdapter(
            onSumar = { producto -> viewModel.modificarStock(producto, 1) },
            onRestar = { producto -> viewModel.modificarStock(producto, -1) }
        )

        binding.rvInventario.layoutManager = LinearLayoutManager(this)
        binding.rvInventario.adapter = adapter

        binding.fabAddProducto.setOnClickListener {
            dialogoNuevoProducto()
        }
    }

    private fun setupObservers() {
        viewModel.productos.observe(this) { lista ->
            adapter.submitList(lista)
        }

        viewModel.error.observe(this) { mensaje ->
            mensaje?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dialogoNuevoProducto() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_nuevo_producto, null)

        val etNombre = view.findViewById<EditText>(R.id.etNombreProducto)
        val etStock = view.findViewById<EditText>(R.id.etStockInicial)
        val etCat = view.findViewById<EditText>(R.id.etCategoria)

        AlertDialog.Builder(this)
            .setTitle("Nuevo Registro de Inventario")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val nom = etNombre.text.toString().trim()
                val stString = etStock.text.toString()
                val st = if (stString.isEmpty()) 0 else stString.toInt()
                val cat = etCat.text.toString().trim()

                if (nom.isNotEmpty()) {
                    viewModel.agregarProducto(nom, st, if (cat.isEmpty()) "Uniformes" else cat)
                } else {
                    Toast.makeText(this, "El nombre es necesario", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
}
