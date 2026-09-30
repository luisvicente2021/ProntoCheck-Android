package com.example.prontocheck.ui.AltaEmpleado

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.prontocheck.data.ml.FaceNetHelper
import com.example.prontocheck.databinding.ActivityAltaEmpleadoBinding
import com.example.prontocheck.data.model.Empleado
import com.example.prontocheck.di.AppDependencies
import com.example.prontocheck.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors



class AltaEmpleadoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAltaEmpleadoBinding
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService
    private var ultimoEmbeddingGenerado: String? = null
    private lateinit var faceNetHelper: FaceNetHelper

    private val viewModel: AltaEmpleadoViewModel by viewModels {
        AltaEmpleadoViewModelFactory(AppDependencies.empleadoRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAltaEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        setupObservers()
        faceNetHelper = FaceNetHelper(this)

        val opciones = listOf("Seleccione Residencial","Aqua", "Altai", "Arbolada", "Cumbres", "Kira", "Palmaris", "Rio")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerResidencial.adapter = adapter

        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            cameraProvider = future.get()
            Log.d("AltaEmpleado", "CameraProvider listo")
        }, ContextCompat.getMainExecutor(this))

        binding.frameIcono.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
            } else {
                activarModoCamara()
            }
        }

        binding.btnCapturarRostro.setOnClickListener {
            capturarYProcesarRostro()
        }

        binding.btnGuardarEmpleado.setOnClickListener {
            ejecutarGuardado()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            activarModoCamara()
        } else {
            Toast.makeText(this, "Se necesita permiso de cámara", Toast.LENGTH_SHORT).show()
        }
    }

    private fun activarModoCamara() {
        binding.frameIcono.visibility = View.GONE
        binding.previewViewAlta.visibility = View.VISIBLE
        binding.btnCapturarRostro.visibility = View.VISIBLE
        binding.tvStatusRostro.text = "Apunta a tu rostro y presiona capturar"
        abrirCamaraParaFoto()
    }

    private fun abrirCamaraParaFoto() {
        val provider = cameraProvider
        if (provider != null) {
            bindCamara(provider)
        } else {
            val future = ProcessCameraProvider.getInstance(this)
            future.addListener({
                val p = future.get()
                cameraProvider = p
                bindCamara(p)
            }, ContextCompat.getMainExecutor(this))
        }
    }

    private fun bindCamara(provider: ProcessCameraProvider) {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.previewViewAlta.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        try {
            provider.unbindAll()
            provider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageCapture
            )
            Log.d("AltaEmpleado", "Cámara iniciada correctamente")
        } catch (e: Exception) {
            Log.e("AltaEmpleado", "Fallo al iniciar cámara: ${e.message}")
        }
    }

    private fun capturarYProcesarRostro() {
        val capture = imageCapture ?: run {
            Toast.makeText(this, "La cámara no está lista", Toast.LENGTH_SHORT).show()
            return
        }

        binding.tvStatusRostro.text = "Procesando rostro..."
        binding.btnCapturarRostro.isEnabled = false

        capture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {

                    val bitmap = imageProxyToBitmap(image)
                    image.close()

                    val detector = com.google.mlkit.vision.face.FaceDetection.getClient(
                        com.google.mlkit.vision.face.FaceDetectorOptions.Builder()
                            .setPerformanceMode(com.google.mlkit.vision.face.FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                            .build()
                    )
                    val mlImage = com.google.mlkit.vision.common.InputImage.fromBitmap(bitmap, 0)

                    detector.process(mlImage)
                        .addOnSuccessListener { faces ->
                            if (faces.isNotEmpty()) {
                                val bounds = faces[0].boundingBox

                                val rostro = recortarRostro(
                                    bitmap,
                                    bounds.left, bounds.top,
                                    bounds.width(), bounds.height()
                                )

                                lifecycleScope.launch {
                                    val embedding = withContext(Dispatchers.Default) {
                                        faceNetHelper.generateEmbedding(rostro)
                                    }

                                    ultimoEmbeddingGenerado = embedding.joinToString(",")

                                    cameraProvider?.unbindAll()

                                    runOnUiThread {
                                        binding.previewViewAlta.visibility = View.GONE
                                        binding.btnCapturarRostro.visibility = View.GONE
                                        binding.btnCapturarRostro.isEnabled = true
                                        binding.frameIcono.visibility = View.VISIBLE
                                        binding.ivEmpleado.setImageResource(android.R.drawable.checkbox_on_background)
                                        binding.ivEmpleado.setColorFilter(Color.GREEN)
                                        binding.tvStatusRostro.text = "¡Rostro capturado correctamente!"
                                        binding.tvStatusRostro.setTextColor(Color.parseColor("#2E7D32"))
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    binding.btnCapturarRostro.isEnabled = true
                                    binding.tvStatusRostro.text = "No se detectó rostro. Intenta de nuevo."
                                    Toast.makeText(this@AltaEmpleadoActivity, "No se detectó rostro", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .addOnFailureListener {
                            runOnUiThread {
                                binding.btnCapturarRostro.isEnabled = true
                                Toast.makeText(this@AltaEmpleadoActivity, "Error al detectar rostro", Toast.LENGTH_SHORT).show()
                            }
                        }
                }

                override fun onError(exception: ImageCaptureException) {
                    binding.btnCapturarRostro.isEnabled = true
                    Log.e("AltaEmpleado", "Error al capturar: ${exception.message}")
                    Toast.makeText(this@AltaEmpleadoActivity, "Error al capturar foto", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val matrix = Matrix()
        matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
        matrix.postScale(-1f, 1f) // Espejo cámara frontal
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun recortarRostro(bitmap: Bitmap, x: Int, y: Int, w: Int, h: Int): Bitmap {
        val nx = x.coerceAtLeast(0)
        val ny = y.coerceAtLeast(0)
        val nw = if (nx + w > bitmap.width) bitmap.width - nx else w
        val nh = if (ny + h > bitmap.height) bitmap.height - ny else h
        return Bitmap.createBitmap(bitmap, nx, ny, nw, nh)
    }

    private fun ejecutarGuardado() {
        Log.d("ALTA_DEBUG", "Embedding a guardar: $ultimoEmbeddingGenerado")

        val empleado = Empleado(
            id = null,
            nombre = binding.etNombre.text.toString().trim(),
            email = binding.etEmail.text.toString().trim(),
            apellido_paterno = binding.etApellidoPaterno.text.toString().trim(),
            apellido_materno = binding.etApellidoMaterno.text.toString().trim(),
            residencial = binding.spinnerResidencial.selectedItem?.toString() ?: "",
            direccion = binding.etDireccion.text.toString().trim(),
            telefono_casa = binding.etTelCasa.text.toString().trim(),
            face_embedding = ultimoEmbeddingGenerado
        )

        if (ultimoEmbeddingGenerado == null) {
            Toast.makeText(this, "Debes capturar la foto del rostro primero", Toast.LENGTH_SHORT).show()
            return
        }

        if (validarCampos(empleado)) {
            viewModel.registrarEmpleado(empleado)
        }
    }

    private fun validarCampos(e: Empleado): Boolean {

        if (
            e.nombre.isBlank() ||
            e.apellido_paterno.isNullOrBlank()
        ) {
            Toast.makeText(
                this,
                "Nombre y Apellido P. son obligatorios",
                Toast.LENGTH_SHORT
            ).show()

            return false
        }

        if (
            e.residencial == "Seleccione Residencial" ||
            e.residencial.isNullOrBlank()
        ) {
            Toast.makeText(
                this,
                "Por favor seleccione una residencial válida",
                Toast.LENGTH_SHORT
            ).show()

            return false
        }

        return true
    }

    private fun setupObservers() {
        viewModel.registroState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnGuardarEmpleado.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "¡Empleado registrado con éxito!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnGuardarEmpleado.isEnabled = true
                    Toast.makeText(this, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        faceNetHelper.close()
    }
}
