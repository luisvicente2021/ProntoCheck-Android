package com.example.prontocheck.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prontocheck.R
import com.example.prontocheck.data.model.Empleado
import com.example.prontocheck.databinding.ActivityGestionPersonalBinding
import com.example.prontocheck.di.AppDependencies
import com.example.prontocheck.ui.AltaEmpleado.AltaEmpleadoActivity
import com.example.prontocheck.utils.Resource

/**
 * Pantalla de gestion de personal.
 *
 * Por que se refactorizo: antes la Activity controlaba paginacion, offsets,
 * estados y llamadas a Supabase. Ahora solo pinta la lista, escucha scroll y
 * navega a alta/edicion.
 *
 * Ventaja: la carga paginada vive en el ViewModel y la red en el Repository; la
 * pantalla queda mas corta, mantenible y menos propensa a duplicar requests.
 */
class GestionPersonalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionPersonalBinding
    private lateinit var adapter: EmpleadosAdapter
    private lateinit var viewModel: GestionPersonalViewModel
    private var reemplazarSiguienteLista = true
    private var primerResume = true

    private val editarLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) reiniciarLista()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionPersonalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupScrollListener()
        setupListeners()
        reiniciarLista()
    }

    override fun onResume() {
        super.onResume()
        if (primerResume) {
            primerResume = false
        } else {
            reiniciarLista()
        }
    }

    private fun setupViewModel() {
        val factory = GestionPersonalViewModelFactory(AppDependencies.empleadoRepository)
        viewModel = ViewModelProvider(this, factory)[GestionPersonalViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.empleados.observe(this) { result ->
            when (result) {
                Resource.Loading -> mostrarEstado("cargando")
                is Resource.Success -> mostrarEmpleados(result.data)
                is Resource.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    mostrarEstado(if (adapter.itemCount == 0) "vacio" else "lista")
                }
            }
        }
    }

    private fun reiniciarLista() {
        reemplazarSiguienteLista = true
        viewModel.reiniciar()
    }

    private fun mostrarEmpleados(empleados: List<Empleado>) {
        if (reemplazarSiguienteLista) {
            adapter.submitList(empleados)
            reemplazarSiguienteLista = false
        } else {
            adapter.agregarMas(empleados)
        }

        mostrarEstado(if (adapter.itemCount == 0) "vacio" else "lista")
    }

    private fun setupRecyclerView() {
        adapter = EmpleadosAdapter { empleado ->
            val intent = Intent(this, EditarEmpleadoActivity::class.java).apply {
                putExtra("id", empleado.id)
                putExtra("nombre", empleado.nombre)
                putExtra("apellido_p", empleado.apellido_paterno)
                putExtra("apellido_m", empleado.apellido_materno)
                putExtra("email", empleado.email)
                putExtra("residencial", empleado.residencial)
                putExtra("direccion", empleado.direccion)
                putExtra("tel_casa", empleado.telefono_casa)
                putExtra("tel_empresa", empleado.telefono_empresa)
                putExtra("latitud", empleado.latitud)
                putExtra("longitud", empleado.longitud)
            }
            editarLauncher.launch(intent)
        }
        binding.recyclerEmpleados.layoutManager = LinearLayoutManager(this)
        binding.recyclerEmpleados.adapter = adapter
    }

    private fun setupScrollListener() {
        binding.recyclerEmpleados.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val ultimoVisible = layoutManager.findLastVisibleItemPosition()
                val total = layoutManager.itemCount

                if (viewModel.puedeCargarMas && ultimoVisible >= total - 3) {
                    viewModel.cargarSiguientePagina()
                }
            }
        })
    }

    private fun setupListeners() {
        binding.btnNuevoRegistro.setOnClickListener {
            startActivity(Intent(this, AltaEmpleadoActivity::class.java))
        }
    }

    private fun mostrarEstado(estado: String) {
        binding.recyclerEmpleados.visibility = if (estado == "lista") View.VISIBLE else View.GONE
        binding.layoutCargando.visibility = if (estado == "cargando") View.VISIBLE else View.GONE
        binding.layoutVacio.visibility = if (estado == "vacio") View.VISIBLE else View.GONE
    }
}

/**
 * Adapter de empleados.
 *
 * Por que esta aqui: es una pieza pequena y especifica de esta pantalla.
 * Ventaja: mantiene el render de cada fila separado de la Activity y permite que
 * la pantalla solo entregue la accion al seleccionar un empleado.
 */
class EmpleadosAdapter(
    private val onEmpleadoSeleccionado: (Empleado) -> Unit
) : RecyclerView.Adapter<EmpleadosAdapter.ViewHolder>() {

    private val lista = mutableListOf<Empleado>()

    fun submitList(nuevaLista: List<Empleado>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    fun agregarMas(nuevos: List<Empleado>) {
        val inicio = lista.size
        lista.addAll(nuevos)
        notifyItemRangeInserted(inicio, nuevos.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_empleado, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val empleado = lista[position]
        holder.bind(empleado)
        holder.itemView.setOnClickListener { onEmpleadoSeleccionado(empleado) }
    }

    override fun getItemCount() = lista.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        private val tvNombreCompleto: TextView = itemView.findViewById(R.id.tvNombreCompleto)
        private val tvIdEmpleado: TextView = itemView.findViewById(R.id.tvIdEmpleado)

        fun bind(empleado: Empleado) {
            tvNombreCompleto.text = "${empleado.nombre} ${empleado.apellido_paterno} ${empleado.apellido_materno}"
            tvIdEmpleado.text = "ID: ${empleado.id}"
            tvAvatar.text = inicialesDe(empleado)
        }

        private fun inicialesDe(empleado: Empleado): String = buildString {
            empleado.nombre.firstOrNull()?.let { append(it.uppercaseChar()) }
            empleado.apellido_paterno?.firstOrNull()?.let { append(it.uppercaseChar()) }
        }
    }
}
