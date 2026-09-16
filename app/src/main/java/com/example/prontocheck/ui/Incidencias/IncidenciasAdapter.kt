package com.example.prontocheck.ui.Incidencias


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prontocheck.data.model.Incidencia
import com.example.prontocheck.databinding.ItemIncidenciaBinding

class IncidenciasAdapter(
    private var lista: List<Incidencia>,
    private val onItemClick: (Incidencia) -> Unit
) : RecyclerView.Adapter<IncidenciasAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemIncidenciaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIncidenciaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        with(holder.binding) {
            // Si no tenemos el nombre (porque viene de otra tabla), mostramos el ID
            tvNombreEmpleado.text = item.nombre_empleado ?: "ID Empleado: ${item.empleado_id.take(8)}..."
            tvMotivo.text = "Motivo: ${item.motivo}"
            tvTipoIncidencia.text = item.tipo.uppercase()
            tvEstadoLabel.text = item.estado

            // Configurar colores según el estado
            when (item.estado) {
                "Pendiente" -> {
                    viewStatusIndicator.setBackgroundColor(Color.parseColor("#FFC107")) // Amarillo
                    tvEstadoLabel.setTextColor(Color.parseColor("#FFC107"))
                }
                "Justificada" -> {
                    viewStatusIndicator.setBackgroundColor(Color.parseColor("#4CAF50")) // Verde
                    tvEstadoLabel.setTextColor(Color.parseColor("#4CAF50"))
                }
                "No Justificada" -> {
                    viewStatusIndicator.setBackgroundColor(Color.parseColor("#F44336")) // Rojo
                    tvEstadoLabel.setTextColor(Color.parseColor("#F44336"))
                }
            }

            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<Incidencia>) {
        this.lista = nuevaLista
        notifyDataSetChanged()
    }
}
