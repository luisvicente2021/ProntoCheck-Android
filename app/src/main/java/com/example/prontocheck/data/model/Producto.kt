package com.example.prontocheck.data.model

import com.google.gson.annotations.SerializedName

data class Producto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("nombre_producto") val nombre: String,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("stock_actual") var stock: Int,
    @SerializedName("unidad_medida") val unidad: String = "piezas"
)