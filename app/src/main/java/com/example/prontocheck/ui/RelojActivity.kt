package com.example.prontocheck

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import com.example.prontocheck.data.ml.FaceNetHelper
import com.example.prontocheck.databinding.ActivityRelojChecadorBinding
import com.example.prontocheck.data.model.Asistencia
import com.example.prontocheck.data.model.PuntoAcceso
import com.example.prontocheck.di.AppDependencies
import com.example.prontocheck.utils.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.example.prontocheck.data.model.Empleado


class RelojActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRelojChecadorBinding
    private lateinit var viewModel: RelojViewModel
    private lateinit var faceNetHelper: FaceNetHelper
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var puntosAcceso: List<PuntoAcceso> = emptyList()
    private var empleadosConRostro: List<Empleado> = emptyList()
    private var puntoSeleccionado: PuntoAcceso? = null
    private var ubicacionValidada = false

    private var miLatitud: Double = 0.0
    private var miLongitud: Double = 0.0
    private var empleadoDetectado: String? = null
    private var idEmpleadoDetectado: String? = null
    private var ultimoTipoRegistro: String? = null
    private var procesando = false

    companion object {
        private const val REQUEST_CAMERA = 2001
        private const val REQUEST_LOCATION = 2002
        private const val UMBRAL_DISTANCIA = 0.6f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRelojChecadorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        faceNetHelper = FaceNetHelper(this)
        cameraExecutor = Executors.newSingleThreadExecutor()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupViewModel()
        setupObservers()
        setupListeners()
        iniciarReloj()
        viewModel.cargarPuntosAcceso()
        viewModel.cargarEmpleadosConRostro()
        mostrarEstadoEspera()
        pedirPermisos()
    }

    private fun setupViewModel() {
        val factory = RelojViewModelFactory(AppDependencies.relojRepository)
        viewModel = ViewModelProvider(this, factory)[RelojViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.puntosAcceso.observe(this) { result ->
            when (result) {
                Resource.Loading -> Unit
                is Resource.Success -> configurarPuntosAcceso(result.data)
                is Resource.Error -> Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.empleados.observe(this) { result ->
            when (result) {
                Resource.Loading -> Unit
                is Resource.Success -> empleadosConRostro = result.data.filter { !it.face_embedding.isNullOrEmpty() }
                is Resource.Error -> Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.registro.observe(this) { result ->
            when (result) {
                Resource.Loading -> Unit
                is Resource.Success -> mostrarRegistroExitoso()
                is Resource.Error -> {
                    Toast.makeText(
                        this,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()

                    idEmpleadoDetectado?.let { empleadoId ->
                        viewModel.obtenerSiguienteMovimiento(empleadoId)
                    }
                }
            }
        }

        viewModel.siguienteMovimiento.observe(this) { result ->
            when (result) {
                Resource.Loading -> {
                    binding.btnEntrada.isEnabled = false
                    binding.btnSalida.isEnabled = false
                    binding.btnEntrada.alpha = 0.5f
                    binding.btnSalida.alpha = 0.5f
                    binding.tvEstado.text = "CONSULTANDO ÚLTIMO REGISTRO..."
                }

                is Resource.Success -> {
                    val siguiente = result.data.lowercase()

                    binding.btnEntrada.isEnabled = siguiente == "entrada"
                    binding.btnSalida.isEnabled = siguiente == "salida"

                    binding.btnEntrada.alpha =
                        if (siguiente == "entrada") 1.0f else 0.5f

                    binding.btnSalida.alpha =
                        if (siguiente == "salida") 1.0f else 0.5f

                    binding.tvEstado.text =
                        "LISTO: ${empleadoDetectado?.uppercase()} - ${siguiente.uppercase()}"
                }

                is Resource.Error -> {
                    binding.btnEntrada.isEnabled = false
                    binding.btnSalida.isEnabled = false

                    Toast.makeText(
                        this,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun pedirPermisos() {
        val permisos = arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
        val faltan = permisos.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (faltan.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, faltan.toTypedArray(), REQUEST_CAMERA)
        }
    }

    private fun setupListeners() {
        binding.btnValidarUbicacion.setOnClickListener { validarUbicacion() }
        binding.btnEscanear.setOnClickListener { if (!procesando && ubicacionValidada) escanearYIdentificar() }

        binding.btnEntrada.setOnClickListener {
            empleadoDetectado?.let { registrarAsistencia("entrada", it) }
        }
        binding.btnSalida.setOnClickListener {
            empleadoDetectado?.let { registrarAsistencia("salida", it) }
        }
    }

    private fun configurarPuntosAcceso(puntos: List<PuntoAcceso>) {
        puntosAcceso = puntos
        val nombres = puntosAcceso.map { it.nombrePunto}
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombres)
        binding.spinnerResidenciales.adapter = adapter
        binding.spinnerResidenciales.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                puntoSeleccionado = puntosAcceso.getOrNull(pos)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun validarUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        binding.tvEstado.text = "OBTENIENDO UBICACIÓN..."
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            1000L
        ).setMaxUpdates(1).build()

        fusedLocationClient.requestLocationUpdates(locationRequest, object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                val location = result.lastLocation ?: return
                miLatitud = location.latitude
                miLongitud = location.longitude

                var puntoEncontrado: PuntoAcceso? = null
                for (punto in puntosAcceso) {
                    val dist = FloatArray(1)
                    Location.distanceBetween(miLatitud, miLongitud, punto.latitud, punto.longitud, dist)
                    if (dist[0] <= (punto.radioMetros + 200)) {
                        puntoEncontrado = punto
                        break
                    }
                }

                if (puntoEncontrado != null) {
                    ubicacionValidada = true
                    puntoSeleccionado = puntoEncontrado
                    binding.ivStatusGps.setImageResource(android.R.drawable.presence_online)
                    binding.ivStatusGps.setColorFilter(Color.GREEN)
                    activarBotonesAsistencia()
                } else {
                    ubicacionValidada = false
                    Toast.makeText(this@RelojActivity, "Fuera de rango", Toast.LENGTH_SHORT).show()
                    activarBotonesAsistencia()
                }
            }
        }, Looper.getMainLooper())
    }

    private fun escanearYIdentificar() {
        val capture = imageCapture ?: return
        procesando = true
        binding.tvEstado.text = "PROCESANDO ROSTRO..."

        capture.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = imageProxyToBitmap(image)
                image.close()
                val detector = FaceDetection.getClient(FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE).build())
                detector.process(InputImage.fromBitmap(bitmap, 0)).addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val b = faces[0].boundingBox
                        compararConEmpleados(recortarRostro(bitmap, b.left, b.top, b.width(), b.height()))
                    } else {
                        mostrarEstadoNoReconocido("No se detectó rostro")
                        procesando = false
                    }
                }
            }
            override fun onError(e: ImageCaptureException) { procesando = false }
        })
    }

    private fun compararConEmpleados(rostro: Bitmap) {
        if (empleadosConRostro.isEmpty()) {
            mostrarEstadoNoReconocido("No hay empleados con rostro cargados")
            viewModel.cargarEmpleadosConRostro()
            procesando = false
            return
        }

        lifecycleScope.launch {
            try {
                val embedding = withContext(Dispatchers.Default) { faceNetHelper.generateEmbedding(rostro) }
                var mejorDist = Float.MAX_VALUE
                var mejorEmp: Empleado? = null

                empleadosConRostro.forEach { emp ->
                    val d = faceNetHelper.calcularDistancia(embedding, faceNetHelper.stringToEmbedding(emp.face_embedding!!))
                    if (d < mejorDist) { mejorDist = d; mejorEmp = emp }
                }

                if (mejorDist <= UMBRAL_DISTANCIA && mejorEmp != null) {
                    idEmpleadoDetectado = mejorEmp.id
                    empleadoDetectado =
                        "${mejorEmp.nombre} ${mejorEmp.apellido_paterno}"

                    mostrarEmpleadoDetectado(
                        empleadoDetectado!!,
                        ((1f - mejorDist) * 100).toInt()
                    )

                    activarBotonesAsistencia()

                    mejorEmp.id?.let { empleadoId ->
                        viewModel.obtenerSiguienteMovimiento(empleadoId)
                    }
                }else {
                    mostrarEstadoNoReconocido("No identificado")
                }
            } catch (e: Exception) { Log.e("Reloj", "Error: ${e.message}") }
            procesando = false
        }
    }

    private fun activarBotonesAsistencia() {
        val gpsOk = ubicacionValidada
        val rostroOk = idEmpleadoDetectado != null
        val puedeMarcar = gpsOk && rostroOk

        runOnUiThread {
            if (gpsOk && binding.previewView.visibility != View.VISIBLE) {
                binding.previewView.visibility = View.VISIBLE
                iniciarCamara()
            }

            binding.btnEntrada.isEnabled = false
            binding.btnSalida.isEnabled = false
            val alpha = 0.5f
            binding.btnEntrada.alpha = alpha
            binding.btnSalida.alpha = alpha

            if (puedeMarcar) binding.tvEstado.text = "LISTO: ${empleadoDetectado?.uppercase()}"
            else if (gpsOk) binding.tvEstado.text = "UBICACIÓN OK: ESCANEE ROSTRO"
        }
    }

    private fun registrarAsistencia(tipo: String, nombre: String) {
        val id = idEmpleadoDetectado
        if (id == null) {
            Toast.makeText(this, "Error: ID no encontrado", Toast.LENGTH_SHORT).show()
            return
        }
        binding.btnEntrada.isEnabled = false
        binding.btnSalida.isEnabled = false
        binding.btnEntrada.alpha = 0.5f
        binding.btnSalida.alpha = 0.5f
        binding.tvEstado.text = "REGISTRANDO ${tipo.uppercase()}..."

        val ahora = Date()
        val asistencia = Asistencia(
            idEmpleadoManual = id,
            nombreEmpleado = nombre,
            tipo = tipo,
            fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(ahora),
            hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(ahora),
            idPuntoAcceso = puntoSeleccionado?.id
        )

        Log.d("ASISTENCIA", "Intentando registrar $tipo para: $nombre con ID: $id")
        ultimoTipoRegistro = tipo
        viewModel.registrarAsistencia(asistencia)
    }

    private fun mostrarRegistroExitoso() {
        binding.tvEstado.text = "${ultimoTipoRegistro?.uppercase()} REGISTRADA"
        Toast.makeText(this, "REGISTRO EXITOSO", Toast.LENGTH_LONG).show()
        binding.btnEntrada.isEnabled = false
        binding.btnSalida.isEnabled = false
        handler.postDelayed({ mostrarEstadoEspera() }, 3000)
    }

    private fun mostrarEstadoEspera() {
        idEmpleadoDetectado = null
        empleadoDetectado = null
        ubicacionValidada = false
        procesando = false
        binding.tvEstado.text = "ESPERANDO UBICACIÓN..."
        binding.ivStatusFace.setImageResource(android.R.drawable.presence_invisible)
        binding.ivStatusFace.setColorFilter(Color.GRAY)
        binding.ivStatusGps.setImageResource(android.R.drawable.btn_radio)
        binding.ivStatusGps.setColorFilter(Color.GRAY)
        binding.previewView.visibility = View.INVISIBLE
        activarBotonesAsistencia()
    }

    private fun mostrarEmpleadoDetectado(nombre: String, confianza: Int) {
        binding.tvEstado.text = "HOLA, $nombre ($confianza%)"
        binding.ivStatusFace.setImageResource(android.R.drawable.presence_online)
        binding.ivStatusFace.setColorFilter(Color.GREEN)
    }

    private fun mostrarEstadoNoReconocido(motivo: String) {
        binding.tvEstado.text = motivo
        binding.ivStatusFace.setImageResource(android.R.drawable.presence_busy)
        binding.ivStatusFace.setColorFilter(Color.RED)
    }

    private fun iniciarCamara() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }
            imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageCapture)
            } catch (e: Exception) { Log.e("Camera", "${e.message}") }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun iniciarReloj() {
        handler.post(object : Runnable {
            override fun run() {
                binding.tvTime.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                binding.tvFullDate.text = SimpleDateFormat("EEEE, dd 'DE' MMMM yyyy", Locale.getDefault()).format(Date()).uppercase()
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val matrix = Matrix().apply {
            postRotate(image.imageInfo.rotationDegrees.toFloat())
            postScale(-1f, 1f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun recortarRostro(bitmap: Bitmap, x: Int, y: Int, w: Int, h: Int): Bitmap {
        val nx = x.coerceAtLeast(0); val ny = y.coerceAtLeast(0)
        return Bitmap.createBitmap(bitmap, nx, ny, w.coerceAtMost(bitmap.width - nx), h.coerceAtMost(bitmap.height - ny))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        faceNetHelper.close()
    }
}
