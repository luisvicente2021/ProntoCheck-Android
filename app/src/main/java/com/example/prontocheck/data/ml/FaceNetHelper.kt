package com.example.prontocheck.data.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class FaceNetHelper(context: Context) {

    private val interpreter: Interpreter
    private val INPUT_SIZE  = 160  // FaceNet espera imágenes de 160x160
    private val OUTPUT_SIZE = 128  // FaceNet genera embeddings de 128 dimensiones

    init {
        interpreter = Interpreter(loadModelFile(context))
    }

    // ─── Carga el modelo .tflite desde assets ────────────────────────────────
    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("mobile_facenet.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    // ─── Genera el embedding de un Bitmap ────────────────────────────────────
    fun generateEmbedding(bitmap: Bitmap): FloatArray {
        val resized    = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val inputBuffer = bitmapToByteBuffer(resized)
        val output     = Array(1) { FloatArray(OUTPUT_SIZE) }
        interpreter.run(inputBuffer, output)
        return normalize(output[0])
    }

    // ─── Convierte Bitmap a ByteBuffer normalizado [-1, 1] ───────────────────
    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
        buffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF)
            val g = ((pixel shr 8)  and 0xFF)
            val b = (pixel          and 0xFF)
            // Normalizar a [-1, 1]
            buffer.putFloat((r - 128f) / 128f)
            buffer.putFloat((g - 128f) / 128f)
            buffer.putFloat((b - 128f) / 128f)
        }
        return buffer
    }

    // ─── Normaliza el vector a longitud 1 ────────────────────────────────────
    private fun normalize(embedding: FloatArray): FloatArray {
        val norm = sqrt(embedding.map { it * it }.sum())
        return if (norm > 0) embedding.map { it / norm }.toFloatArray()
        else embedding
    }

    // ─── Calcula distancia coseno entre dos embeddings ───────────────────────
    // Distancia < 0.6 = misma persona
    fun calcularDistancia(emb1: FloatArray, emb2: FloatArray): Float {
        var dot   = 0f
        var norm1 = 0f
        var norm2 = 0f
        for (i in emb1.indices) {
            dot   += emb1[i] * emb2[i]
            norm1 += emb1[i] * emb1[i]
            norm2 += emb2[i] * emb2[i]
        }
        val cosine = dot / (sqrt(norm1) * sqrt(norm2))
        return 1f - cosine // Distancia: 0 = idéntico, 2 = opuesto
    }

    // ─── Convierte embedding a String para guardar en Supabase ───────────────
    fun embeddingToString(embedding: FloatArray): String =
        embedding.joinToString(",")

    // ─── Convierte String de Supabase a FloatArray ───────────────────────────
    fun stringToEmbedding(str: String): FloatArray =
        str.split(",").map { it.toFloat() }.toFloatArray()

    fun close() {
        interpreter.close()
    }
}