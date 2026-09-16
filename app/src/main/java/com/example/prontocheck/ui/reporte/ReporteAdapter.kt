package com.example.prontocheck.ui.reporte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prontocheck.R


// ESTE IMPORT ES VITAL: Debe apuntar a donde creaste la clase ResumenEmpleado
import com.example.prontocheck.data.model.ResumenEmpleado

class ReporteAdapter : RecyclerView.Adapter<ReporteAdapter.ViewHolder>() {

    private val lista = mutableListOf<ResumenEmpleado>()

    fun submitList(nuevaLista: List<ResumenEmpleado>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reporte_empleado, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(lista[position])

    override fun getItemCount() = lista.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombreEmpleado)
        private val tvId: TextView = itemView.findViewById(R.id.tvIdEmpleado)
        private val tvDias: TextView = itemView.findViewById(R.id.tvDiasAsistidos)
        private val tvDetalle: TextView = itemView.findViewById(R.id.tvDetalleAsistencia)
        private val tvTotalHoras: TextView = itemView.findViewById(R.id.tvTotalHoras)

        fun bind(resumen: ResumenEmpleado) {
            tvNombre.text = resumen.nombre
            // Nota: Si en tu modelo ResumenEmpleado no pusiste 'idEmpleado',
            // usa solo resumen.residencial o el campo que tengas disponible.
            tvId.text = "${resumen.residencial}"
            tvDias.text = "${resumen.diasAsistidos}"
            tvDetalle.text = resumen.detalles.joinToString("\n")
            tvTotalHoras.text = resumen.totalHoras

            // Generar iniciales para el avatar (Ej: Juan Perez -> JP)
            val iniciales = resumen.nombre.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .map { it.first().uppercaseChar() }
                .joinToString("")
            tvAvatar.text = iniciales
        }
    }
}