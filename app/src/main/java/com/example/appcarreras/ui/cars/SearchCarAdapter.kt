package com.example.appcarreras.ui.cars

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.appcarreras.R
import com.example.appcarreras.data.dao.CocheDao

class SearchCarAdapter(
    private val coches: List<CocheDao.CocheBusqueda>,
    private val onCocheClick: (CocheDao.CocheBusqueda) -> Unit
) : RecyclerView.Adapter<SearchCarAdapter.SearchCarViewHolder>() {

    private var cocheSeleccionado: CocheDao.CocheBusqueda? = null

    inner class SearchCarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDorsal: TextView = view.findViewById(R.id.tvDorsal)
        val tvCarName: TextView = view.findViewById(R.id.tvCarName)
        val tvTeamName: TextView = view.findViewById(R.id.tvTeamName)
        val cardView: com.google.android.material.card.MaterialCardView = 
            view.findViewById(R.id.cardSearchCar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchCarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_car, parent, false)
        return SearchCarViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchCarViewHolder, position: Int) {
        val coche = coches[position]
        val isSelected = cocheSeleccionado?.let { 
            it.marca == coche.marca && 
            it.modelo == coche.modelo && 
            it.color == coche.color && 
            it.dorsal == coche.dorsal 
        } ?: false

        holder.tvDorsal.text = coche.dorsal.toString()
        holder.tvCarName.text = "${coche.marca} ${coche.modelo}"
        holder.tvTeamName.text = holder.itemView.context.getString(
            com.example.appcarreras.R.string.label_color_prefix, 
            coche.color
        )

        // Cambiar apariencia visual si está seleccionado
        if (isSelected) {
            // Seleccionado: borde naranja grueso, elevación alta, fondo ligeramente más claro
            holder.cardView.strokeWidth = 4 // Borde grueso
            holder.cardView.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.primary_orange)
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.card_background)
            )
            holder.cardView.cardElevation = 8f // Mayor elevación
            holder.tvCarName.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.primary_orange)
            )
            holder.tvTeamName.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
            holder.tvDorsal.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.primary_orange)
            )
        } else {
            // No seleccionado: sin borde, elevación normal
            holder.cardView.strokeWidth = 0 // Sin borde
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.card_background)
            )
            holder.cardView.cardElevation = 2f // Elevación normal
            holder.tvCarName.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
            holder.tvTeamName.setTextColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)
            )
            holder.tvDorsal.setTextColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.holo_orange_dark)
            )
        }

        holder.itemView.setOnClickListener {
            onCocheClick(coche)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = coches.size

    fun setSelectedCoche(coche: CocheDao.CocheBusqueda?) {
        cocheSeleccionado = coche
        notifyDataSetChanged()
    }
}

