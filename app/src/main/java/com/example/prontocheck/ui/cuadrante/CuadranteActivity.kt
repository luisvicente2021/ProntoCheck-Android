package com.example.prontocheck.ui.cuadrante



import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.prontocheck.databinding.ActivityCuadranteBinding
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import java.util.Calendar
import com.example.prontocheck.utils.Resource
import com.example.prontocheck.di.AppDependencies



class CuadranteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCuadranteBinding
    private lateinit var viewModel: CuadranteViewModel

    private var empleados: List<com.example.prontocheck.data.model.Empleado> = emptyList()

    private var mesActual  = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var anioActual = Calendar.getInstance().get(Calendar.YEAR)

    // clave = "empleadoId_dia"  →  conjunto de turnos asignados ese día
    private val turnosAsignados = mutableMapOf<String, MutableSet<String>>()

    private val nombresMeses = listOf(
        "Enero","Febrero","Marzo","Abril","Mayo","Junio",
        "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
    )

    private val residenciales = listOf("Todas", "Altai", "Versalles", "Piamonte", "Castilla")

    // ─── LIFECYCLE ────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCuadranteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupObservers()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.btnMesAnterior.setOnClickListener {
            if (mesActual == 1) { mesActual = 12; anioActual-- } else mesActual--
            actualizarVista()
        }

        binding.btnMesSiguiente.setOnClickListener {
            if (mesActual == 12) { mesActual = 1; anioActual++ } else mesActual++
            actualizarVista()
        }

        binding.btnGuardarCuadrante.setOnClickListener {
            guardarCuadrante()
        }

        configurarSpinnerResidencial()
    }

    private fun setupViewModel() {
        val factory = CuadranteViewModelFactory(AppDependencies.cuadranteRepository)
        viewModel = ViewModelProvider(this, factory)[CuadranteViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.empleados.observe(this) { result ->
            when (result) {
                Resource.Loading -> Unit
                is Resource.Success -> {
                    empleados = result.data
                    turnosAsignados.clear()
                    actualizarVista()
                }
                is Resource.Error -> Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── SPINNER RESIDENCIAL ──────────────────────────────────────────────────

    private fun configurarSpinnerResidencial() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, residenciales)
        binding.spinnerResidencial.adapter = adapter

        binding.spinnerResidencial.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: android.view.View?, pos: Int, p3: Long) {
                cargarEmpleados(residenciales[pos])
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    // ─── CARGA DE EMPLEADOS ───────────────────────────────────────────────────

    private fun cargarEmpleados(residencial: String = "Todas") {
        viewModel.cargarEmpleados(residencial)
    }

    // ─── VISTA ────────────────────────────────────────────────────────────────

    private fun actualizarVista() {
        binding.tvMesAnio.text = "${nombresMeses[mesActual - 1]} $anioActual"
        construirTabla()
    }

    private fun construirTabla() {
        binding.tablaCuadrante.removeAllViews()

        if (empleados.isEmpty()) {
            Toast.makeText(this, "No hay empleados para mostrar", Toast.LENGTH_SHORT).show()
            return
        }

        val diasEnMes = diasDelMes(mesActual, anioActual)
        val cellSize  = dpToPx(48)
        val nameWidth = dpToPx(130)

        // ── CABECERA DE DÍAS ─────────────────────────────────────────────────
        val filaCabecera = TableRow(this)
        filaCabecera.addView(
            celdaTexto("EMPLEADO", nameWidth, cellSize, bold = true, bgColor = "#1A1A2E", textColor = "#FFFFFF")
        )

        for (dia in 1..diasEnMes) {
            val cal = Calendar.getInstance()
            cal.set(anioActual, mesActual - 1, dia)
            val nombreDia  = listOf("D","L","M","X","J","V","S")[cal.get(Calendar.DAY_OF_WEEK) - 1]
            val esFinSemana = cal.get(Calendar.DAY_OF_WEEK) in listOf(Calendar.SATURDAY, Calendar.SUNDAY)

            val contenedor = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity     = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(cellSize, cellSize)
                setBackgroundColor(Color.parseColor(if (esFinSemana) "#2C2C54" else "#1A1A2E"))
            }
            contenedor.addView(TextView(this).apply {
                text = "$dia"; textSize = 10f
                setTextColor(Color.WHITE); gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
            })
            contenedor.addView(TextView(this).apply {
                text = nombreDia; textSize = 9f
                setTextColor(Color.parseColor("#AAAAAA")); gravity = Gravity.CENTER
            })
            filaCabecera.addView(contenedor)
        }
        binding.tablaCuadrante.addView(filaCabecera)

        // ── FILAS DE EMPLEADOS ───────────────────────────────────────────────
        empleados.forEachIndexed { index, empleado ->
            val fila   = TableRow(this)
            val bgFila = if (index % 2 == 0) "#FFFFFF" else "#F3F4F6"

            val nombreCorto = "${empleado.nombre} ${empleado.apellido_paterno?.firstOrNull() ?: ""}."
            fila.addView(
                celdaTexto(nombreCorto, nameWidth, cellSize, bold = false, bgColor = bgFila, textColor = "#1A1A2E")
            )

            for (dia in 1..diasEnMes) {
                val clave = "${empleado.id}_${dia}"
                fila.addView(crearCeldaMultiTurno(clave, cellSize, bgFila, empleado, dia))
            }
            binding.tablaCuadrante.addView(fila)
        }
    }

    // ─── CELDA MULTI-TURNO ────────────────────────────────────────────────────

    private fun crearCeldaMultiTurno(
        clave: String,
        cellSize: Int,
        bgFila: String,
        empleado: com.example.prontocheck.data.model.Empleado,
        dia: Int
    ): FrameLayout {

        val frame = FrameLayout(this).apply {
            layoutParams = TableRow.LayoutParams(cellSize, cellSize).apply {
                setMargins(1, 1, 1, 1)
            }
        }

        // Texto principal del turno
        val tvTurno = TextView(this).apply {
            val turnos = turnosAsignados[clave] ?: emptySet()
            text      = if (turnos.isEmpty()) "-" else turnos.joinToString("+")
            textSize  = if ((turnosAsignados[clave]?.size ?: 0) > 1) 8f else 11f
            gravity   = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            aplicarColorMultiTurno(this, turnosAsignados[clave] ?: emptySet(), bgFila)
        }

        // Botón "+" esquina superior derecha
        val btnMas = TextView(this).apply {
            text = "+"
            textSize = 9f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#444444"))
            layoutParams = FrameLayout.LayoutParams(dpToPx(15), dpToPx(15)).apply {
                gravity = Gravity.TOP or Gravity.END
            }
        }

        frame.addView(tvTurno)
        frame.addView(btnMas)

        // Toque en celda vacía → asigna T1 directo
        tvTurno.setOnClickListener {
            val turnos = turnosAsignados[clave]
            if (turnos.isNullOrEmpty()) {
                turnosAsignados[clave] = mutableSetOf("T1")
                refrescarCelda(frame, clave, bgFila)
            }
        }

        // Toque en "+" → dialog con checkboxes
        btnMas.setOnClickListener {
            mostrarDialogTurnos(clave, bgFila, empleado, dia, frame)
        }

        return frame
    }

    // ─── DIALOG DE TURNOS ─────────────────────────────────────────────────────

    private fun mostrarDialogTurnos(
        clave: String,
        bgFila: String,
        empleado: com.example.prontocheck.data.model.Empleado,
        dia: Int,
        frame: FrameLayout
    ) {
        val turnosActuales = turnosAsignados[clave]?.toMutableSet() ?: mutableSetOf()
        val opciones = arrayOf("T1 — Mañana (07:00–15:00)", "T2 — Tarde (15:00–23:00)", "T3 — Noche (23:00–07:00)", "D — Descanso")
        val claves   = arrayOf("T1", "T2", "T3", "D")
        val checked  = BooleanArray(claves.size) { turnosActuales.contains(claves[it]) }

        val nombre = "${empleado.nombre} ${empleado.apellido_paterno ?: ""}"

        AlertDialog.Builder(this)
            .setTitle("$nombre\nDia $dia de ${nombresMeses[mesActual - 1]} $anioActual")
            .setMultiChoiceItems(opciones, checked) { _, which, isChecked ->
                if (isChecked) {
                    // Descanso es exclusivo: limpia todo lo demás
                    if (claves[which] == "D") {
                        turnosActuales.clear()
                    } else {
                        // Si había descanso, lo quita al poner un turno
                        turnosActuales.remove("D")
                    }
                    turnosActuales.add(claves[which])
                } else {
                    turnosActuales.remove(claves[which])
                }
            }
            .setPositiveButton("Aplicar") { _, _ ->
                if (turnosActuales.isEmpty()) {
                    turnosAsignados.remove(clave)
                } else {
                    turnosAsignados[clave] = turnosActuales
                }
                refrescarCelda(frame, clave, bgFila)
            }
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Limpiar") { _, _ ->
                turnosAsignados.remove(clave)
                refrescarCelda(frame, clave, bgFila)
            }
            .show()
    }

    // ─── HELPERS DE UI ────────────────────────────────────────────────────────

    private fun refrescarCelda(frame: FrameLayout, clave: String, bgFila: String) {
        val turnos  = turnosAsignados[clave] ?: emptySet()
        val tvTurno = frame.getChildAt(0) as? TextView ?: return
        tvTurno.text     = if (turnos.isEmpty()) "-" else turnos.joinToString("+")
        tvTurno.textSize = if (turnos.size > 1) 8f else 11f
        aplicarColorMultiTurno(tvTurno, turnos, bgFila)
    }

    private fun aplicarColorMultiTurno(tv: TextView, turnos: Set<String>, bgDefault: String) {
        when {
            turnos.isEmpty()      -> { tv.setBackgroundColor(Color.parseColor(bgDefault));  tv.setTextColor(Color.parseColor("#9CA3AF")) }
            turnos.contains("D")  -> { tv.setBackgroundColor(Color.parseColor("#F44336"));  tv.setTextColor(Color.WHITE) }
            turnos.size > 1       -> { tv.setBackgroundColor(Color.parseColor("#7B1FA2"));  tv.setTextColor(Color.WHITE) } // Morado = doblete
            turnos.contains("T1") -> { tv.setBackgroundColor(Color.parseColor("#4CAF50"));  tv.setTextColor(Color.WHITE) }
            turnos.contains("T2") -> { tv.setBackgroundColor(Color.parseColor("#FF9800"));  tv.setTextColor(Color.WHITE) }
            turnos.contains("T3") -> { tv.setBackgroundColor(Color.parseColor("#3F51B5"));  tv.setTextColor(Color.WHITE) }
            else                  -> { tv.setBackgroundColor(Color.parseColor(bgDefault));  tv.setTextColor(Color.parseColor("#9CA3AF")) }
        }
    }

    private fun celdaTexto(
        texto: String, ancho: Int, alto: Int,
        bold: Boolean, bgColor: String, textColor: String
    ): TextView {
        return TextView(this).apply {
            text = texto
            textSize = 11f
            gravity  = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(ancho, alto)
            setBackgroundColor(Color.parseColor(bgColor))
            setTextColor(Color.parseColor(textColor))
            if (bold) setTypeface(null, Typeface.BOLD)
            setPadding(4, 0, 4, 0)
        }
    }

    // ─── GUARDAR ─────────────────────────────────────────────────────────────

    private fun guardarCuadrante() {
        val asignados = turnosAsignados.filter { it.value.isNotEmpty() }

        if (asignados.isEmpty()) {
            Toast.makeText(this, "No hay turnos asignados aún", Toast.LENGTH_SHORT).show()
            return
        }

        // Resumen antes de conectar el endpoint
        val dobles   = asignados.count { it.value.size > 1 }
        val normales = asignados.count { it.value.size == 1 }
        Toast.makeText(
            this,
            "$normales turnos simples, $dobles dobletes listos para guardar.",
            Toast.LENGTH_LONG
        ).show()

        // Cuando exista el endpoint de cuadrantes, la persistencia debe vivir en
        // CuadranteRepository y exponerse desde CuadranteViewModel.
    }

    // ─── UTILIDADES ───────────────────────────────────────────────────────────

    private fun diasDelMes(mes: Int, anio: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(anio, mes - 1, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
        ).toInt()
}
