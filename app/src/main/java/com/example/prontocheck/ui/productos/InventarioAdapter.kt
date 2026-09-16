package com.example.prontocheck.ui.productos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prontocheck.data.model.Producto
import com.example.prontocheck.databinding.ItemInventarioBinding

class InventarioAdapter(
    private val onSumar: (Producto) -> Unit,
    private val onRestar: (Producto) -> Unit
) : RecyclerView.Adapter<InventarioAdapter.ViewHolder>() {

    private var productos = listOf<Producto>()

    fun submitList(nuevaLista: List<Producto>) {
        productos = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInventarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productos[position])
    }

    override fun getItemCount() = productos.size

    inner class ViewHolder(private val binding: ItemInventarioBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(producto: Producto) {
            binding.tvNombreProducto.text = producto.nombre
            binding.tvCategoria.text = producto.categoria
            binding.tvStock.text = producto.stock.toString()

            // Toque Senior: Alerta visual si no hay stock
            if (producto.stock <= 0) {
                binding.tvStock.setTextColor(Color.RED)
            } else {
                binding.tvStock.setTextColor(Color.parseColor("#1A1A2E"))
            }

            binding.btnSumar.setOnClickListener { onSumar(producto) }
            binding.btnRestar.setOnClickListener { onRestar(producto) }
        }
    }
}
