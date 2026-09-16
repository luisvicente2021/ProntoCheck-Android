package com.example.prontocheck.ui.productos


import com.example.prontocheck.data.model.Producto
import com.example.prontocheck.data.network.SupabaseApi

class InventarioRepository(private val api: SupabaseApi) {
    suspend fun fetchInventario() = api.getInventario()

    suspend fun updateStock(id: String, nuevoStock: Int) =
        api.actualizarStock("eq.$id", mapOf("stock_actual" to nuevoStock))

    suspend fun saveProducto(producto: Producto) =
        api.insertarProducto(producto)
}
