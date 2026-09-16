package com.example.prontocheck.ui.productos

import androidx.lifecycle.*
import com.example.prontocheck.data.model.Producto
import kotlinx.coroutines.launch

class InventarioViewModel(private val repository: InventarioRepository) : ViewModel() {

    private val _productos = MutableLiveData<List<Producto>>()
    val productos: LiveData<List<Producto>> = _productos

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun cargarInventario() {
        viewModelScope.launch {
            val response = repository.fetchInventario()
            if (response.isSuccessful) _productos.value = response.body()
            else _error.value = "Error al cargar datos"
        }
    }

    fun modificarStock(producto: Producto, cambio: Int) {
        val nuevoValor = producto.stock + cambio
        if (nuevoValor < 0) return // Seguridad: no stock negativo

        viewModelScope.launch {
            val response = repository.updateStock(producto.id!!, nuevoValor)
            if (response.isSuccessful) {
                // Actualización reactiva local
                val listaActual = _productos.value?.toMutableList() ?: mutableListOf()
                val index = listaActual.indexOfFirst { it.id == producto.id }
                if (index != -1) {
                    listaActual[index] = producto.copy(stock = nuevoValor)
                    _productos.value = listaActual
                }
            }
        }
    }

    fun agregarProducto(nombre: String, stock: Int, categoria: String) {
        val nuevo = Producto(nombre = nombre, stock = stock, categoria = categoria)
        viewModelScope.launch {
            if (repository.saveProducto(nuevo).isSuccessful) cargarInventario()
            else _error.value = "No se pudo crear el producto"
        }
    }
}