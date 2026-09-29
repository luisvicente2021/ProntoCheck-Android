package com.example.prontocheck.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prontocheck.R
import com.example.prontocheck.data.model.Empleado
import com.example.prontocheck.databinding.ActivityGestionPersonalBinding
import com.example.prontocheck.di.AppDependencies
import com.example.prontocheck.ui.AltaEmpleado.AltaEmpleadoActivity
import com.example.prontocheck.ui.detalles.DetalleEmpleadoActivity
import com.example.prontocheck.utils.Resource

class GestionPersonalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionPersonalBinding
    private lateinit var adapter: EmpleadosAdapter
    private lateinit var viewModel: GestionPersonalViewModel

    private var reemplazarSiguienteLista = true
    private var primerResume = true

    private val editarLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {
                reiniciarLista()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityGestionPersonalBinding.inflate(layoutInflater)

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

        val factory =
            GestionPersonalViewModelFactory(
                AppDependencies.empleadoRepository
            )

        viewModel =
            ViewModelProvider(
                this,
                factory
            )[GestionPersonalViewModel::class.java]
    }

    private fun setupObservers() {

        // LISTA DE EMPLEADOS
        viewModel.empleados.observe(this) { result ->

            when (result) {

                Resource.Loading -> {
                    mostrarEstado("cargando")
                }

                is Resource.Success -> {
                    mostrarEmpleados(result.data)
                }

                is Resource.Error -> {

                    Toast.makeText(
                        this,
                        result.message,
                        Toast.LENGTH_SHORT
                    ).show()

                    mostrarEstado(
                        if (adapter.itemCount == 0)
                            "vacio"
                        else
                            "lista"
                    )
                }
            }
        }

        // RESULTADO DE DAR DE BAJA
        viewModel.eliminarResultado.observe(this) { result ->

            when (result) {

                Resource.Loading -> {
                    // Por ahora no hacemos nada.
                }

                is Resource.Success -> {

                    Toast.makeText(
                        this,
                        "Empleado dado de baja correctamente",
                        Toast.LENGTH_SHORT
                    ).show()

                    reiniciarLista()
                }

                is Resource.Error -> {

                    Toast.makeText(
                        this,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun reiniciarLista() {

        reemplazarSiguienteLista = true
        viewModel.reiniciar()
    }

    private fun mostrarEmpleados(
        empleados: List<Empleado>
    ) {

        if (reemplazarSiguienteLista) {

            adapter.submitList(empleados)
            reemplazarSiguienteLista = false

        } else {

            adapter.agregarMas(empleados)
        }

        mostrarEstado(
            if (adapter.itemCount == 0)
                "vacio"
            else
                "lista"
        )
    }

    private fun setupRecyclerView() {

        adapter = EmpleadosAdapter(
            onVerDetalle = { empleado ->
                abrirDetalleEmpleado(empleado)
            }
        )

        binding.recyclerEmpleados.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerEmpleados.adapter =
            adapter
    }

    /**
     * Abre la pantalla con la información completa del empleado.
     */
    private fun abrirDetalleEmpleado(
        empleado: Empleado
    ) {

        val intent =
            Intent(
                this,
                DetalleEmpleadoActivity::class.java
            ).apply {

                putExtra("id", empleado.id)
                putExtra("nombre", empleado.nombre)

                putExtra(
                    "apellido_p",
                    empleado.apellido_paterno
                )

                putExtra(
                    "apellido_m",
                    empleado.apellido_materno
                )

                putExtra(
                    "email",
                    empleado.email
                )

                putExtra(
                    "residencial",
                    empleado.residencial
                )

                putExtra(
                    "direccion",
                    empleado.direccion
                )

                putExtra(
                    "tel_casa",
                    empleado.telefono_casa
                )

                putExtra(
                    "tel_empresa",
                    empleado.telefono_empresa
                )

                putExtra(
                    "latitud",
                    empleado.latitud
                )

                putExtra(
                    "longitud",
                    empleado.longitud
                )

                putExtra(
                    "jornada_horas",
                    empleado.jornada_horas
                )

                putExtra(
                    "activo",
                    empleado.activo
                )
            }

        startActivity(intent)
    }

    /**
     * Abre la pantalla para editar el empleado.
     */
    private fun abrirEditarEmpleado(
        empleado: Empleado
    ) {

        val intent =
            Intent(
                this,
                EditarEmpleadoActivity::class.java
            ).apply {

                putExtra("id", empleado.id)
                putExtra("nombre", empleado.nombre)

                putExtra(
                    "apellido_p",
                    empleado.apellido_paterno
                )

                putExtra(
                    "apellido_m",
                    empleado.apellido_materno
                )

                putExtra(
                    "email",
                    empleado.email
                )

                putExtra(
                    "residencial",
                    empleado.residencial
                )

                putExtra(
                    "direccion",
                    empleado.direccion
                )

                putExtra(
                    "tel_casa",
                    empleado.telefono_casa
                )

                putExtra(
                    "tel_empresa",
                    empleado.telefono_empresa
                )

                putExtra(
                    "latitud",
                    empleado.latitud
                )

                putExtra(
                    "longitud",
                    empleado.longitud
                )
            }

        editarLauncher.launch(intent)
    }

    /**
     * Confirma la baja lógica del empleado.
     *
     * El empleado NO se elimina físicamente de Supabase.
     * Se cambia activo = false.
     */
    private fun confirmarDarDeBajaEmpleado(
        empleado: Empleado
    ) {

        val nombreCompleto =
            "${empleado.nombre} " +
                    "${empleado.apellido_paterno ?: ""} " +
                    "${empleado.apellido_materno ?: ""}"

        AlertDialog.Builder(this)
            .setTitle("Dar de baja empleado")
            .setMessage(
                "¿Estás seguro de que deseas dar de baja a " +
                        "${nombreCompleto.trim()}?"
            )
            .setNegativeButton(
                "Cancelar",
                null
            )
            .setPositiveButton(
                "Dar de baja"
            ) { _, _ ->

                val idEmpleado = empleado.id

                if (idEmpleado != null) {

                    viewModel.eliminarEmpleado(idEmpleado)

                } else {

                    Toast.makeText(
                        this,
                        "No se puede dar de baja: el empleado no tiene ID",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    private fun setupScrollListener() {

        binding.recyclerEmpleados.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {

                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {

                    super.onScrolled(
                        recyclerView,
                        dx,
                        dy
                    )

                    val layoutManager =
                        recyclerView.layoutManager
                                as LinearLayoutManager

                    val ultimoVisible =
                        layoutManager
                            .findLastVisibleItemPosition()

                    val total =
                        layoutManager.itemCount

                    if (
                        viewModel.puedeCargarMas &&
                        ultimoVisible >= total - 3
                    ) {

                        viewModel.cargarSiguientePagina()
                    }
                }
            }
        )
    }

    private fun setupListeners() {

        binding.btnNuevoRegistro
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        AltaEmpleadoActivity::class.java
                    )
                )
            }
    }

    private fun mostrarEstado(
        estado: String
    ) {

        binding.recyclerEmpleados.visibility =
            if (estado == "lista")
                View.VISIBLE
            else
                View.GONE

        binding.layoutCargando.visibility =
            if (estado == "cargando")
                View.VISIBLE
            else
                View.GONE

        binding.layoutVacio.visibility =
            if (estado == "vacio")
                View.VISIBLE
            else
                View.GONE
    }
}


/**
 * Adapter de empleados.
 *
 * Acciones:
 * - Avatar / nombre / ID -> ver detalle
 * - Lápiz -> editar
 * - Papelera -> dar de baja
 */
class EmpleadosAdapter(
private val onVerDetalle: (Empleado) -> Unit
) : RecyclerView.Adapter<EmpleadosAdapter.ViewHolder>() {

    private val lista =
        mutableListOf<Empleado>()

    fun submitList(
        nuevaLista: List<Empleado>
    ) {

        lista.clear()
        lista.addAll(nuevaLista)

        notifyDataSetChanged()
    }

    fun agregarMas(
        nuevos: List<Empleado>
    ) {

        val inicio =
            lista.size

        lista.addAll(nuevos)

        notifyItemRangeInserted(
            inicio,
            nuevos.size
        )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_empleado,
                    parent,
                    false
                )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        holder.bind(
            lista[position]
        )
    }

    override fun getItemCount(): Int =
        lista.size

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvAvatar: TextView =
            itemView.findViewById(
                R.id.tvAvatar
            )

        private val tvNombreCompleto: TextView =
            itemView.findViewById(
                R.id.tvNombreCompleto
            )

        private val tvIdEmpleado: TextView =
            itemView.findViewById(
                R.id.tvIdEmpleado
            )


        fun bind(
            empleado: Empleado
        ) {

            val nombreCompleto =
                "${empleado.nombre} " +
                        "${empleado.apellido_paterno ?: ""} " +
                        "${empleado.apellido_materno ?: ""}"

            tvNombreCompleto.text =
                nombreCompleto.trim()

            tvIdEmpleado.text =
                "ID: ${empleado.id}"

            tvAvatar.text =
                inicialesDe(empleado)

            // VER DETALLE AL TOCAR EL AVATAR
            tvAvatar.setOnClickListener {
                onVerDetalle(empleado)
            }

            // VER DETALLE AL TOCAR EL NOMBRE
            tvNombreCompleto.setOnClickListener {
                onVerDetalle(empleado)
            }

            // VER DETALLE AL TOCAR EL ID
            tvIdEmpleado.setOnClickListener {
                onVerDetalle(empleado)
            }

        }

        private fun inicialesDe(
            empleado: Empleado
        ): String =
            buildString {

                empleado.nombre
                    .firstOrNull()
                    ?.let {
                        append(
                            it.uppercaseChar()
                        )
                    }

                empleado.apellido_paterno
                    ?.firstOrNull()
                    ?.let {
                        append(
                            it.uppercaseChar()
                        )
                    }
            }
    }
}