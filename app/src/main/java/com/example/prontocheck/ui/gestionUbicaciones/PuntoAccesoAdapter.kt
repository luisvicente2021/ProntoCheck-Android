package com.example.prontocheck.ui.gestionUbicaciones

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prontocheck.data.model.PuntoAcceso
import com.example.prontocheck.databinding.ItemPuntoAccesoBinding

class PuntoAccesoAdapter(
    private var puntos: List<PuntoAcceso>,
    private val onEliminarClick: (PuntoAcceso) -> Unit
) : RecyclerView.Adapter<PuntoAccesoAdapter.ViewHolder>() {

    class ViewHolder(
        val binding: ItemPuntoAccesoBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding = ItemPuntoAccesoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val punto = puntos[position]

        holder.binding.tvNombreResidencial.text =
            punto.nombreResidencial ?: "Sin residencial"

        holder.binding.tvNombrePunto.text =
            punto.nombrePunto

        holder.binding.tvCoordenadas.text =
            "Lat: ${punto.latitud}, Lon: ${punto.longitud}\n" +
                    "Radio: ${punto.radioMetros} m"

        holder.binding.btnEliminarPunto.setOnClickListener {
            onEliminarClick(punto)
        }
    }

    override fun getItemCount(): Int {
        return puntos.size
    }

    fun actualizarLista(
        nuevaLista: List<PuntoAcceso>
    ) {
        puntos = nuevaLista
        notifyDataSetChanged()
    }
}