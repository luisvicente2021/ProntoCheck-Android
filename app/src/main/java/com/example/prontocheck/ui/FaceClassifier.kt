package com.example.prontocheck.ui

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.FileInputStream


import java.nio.channels.FileChannel



class FaceClassifier(context: Context) {
    private var interpreter: Interpreter? = null
    private var inputSize = 112 // Se detecta automáticamente al iniciar

    init {
        val modelFile = context.assets.openFd("mobile_facenet.tflite")
        val inputStream = FileInputStream(modelFile.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = modelFile.startOffset
        val declaredLength = modelFile.declaredLength
        val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        interpreter = Interpreter(modelBuffer)

        // ✅ Detectar automáticamente el tamaño que espera el modelo
        val inputShape = interpreter!!.getInputTensor(0).shape()
        inputSize = inputShape[1] // [1, altura, ancho, 3] → tomamos altura
        Log.d("FaceClassifier", "Modelo espera: ${inputShape.contentToString()}")
    }

    fun extraerEmbedding(bitmap: Bitmap): FloatArray {
        // ✅ Detectar tamaño de salida automáticamente también
        val outputSize = interpreter!!.getOutputTensor(0).shape()[1]
        val output = Array(1) { FloatArray(outputSize) }

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(127.5f, 127.5f))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        interpreter?.run(tensorImage.buffer, output)
        return output[0]
    }

    fun cerrar() {
        interpreter?.close()
    }
}