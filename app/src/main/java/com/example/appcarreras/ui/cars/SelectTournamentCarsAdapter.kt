package com.example.appcarreras.ui.cars

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.appcarreras.R
import com.example.appcarreras.data.entity.CocheEntity
import com.example.appcarreras.ui.cars.CarStatus

class SelectTournamentCarsAdapter(
    private val coches: MutableList<CocheEntity>,
    private val cochesSeleccionados: MutableSet<Int>,
    private val onCocheClick: (Int) -> Unit
) : RecyclerView.Adapter<SelectTournamentCarsAdapter.SelectCarViewHolder>() {

    private var cochesFiltrados: MutableList<CocheEntity> = mutableListOf()

    inner class SelectCarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDorsal: android.widget.TextView = view.findViewById(R.id.tvDorsal)
        val carName: android.widget.TextView = view.findViewById(R.id.tvCarName)
        val teamName: android.widget.TextView = view.findViewById(R.id.tvTeamName)
        val statusDot: View = view.findViewById(R.id.statusDot)
        val cardView: com.google.android.material.card.MaterialCardView = 
            view.findViewById(R.id.cardCar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectCarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car, parent, false)
        return SelectCarViewHolder(view)
    }

    init {
        actualizarListaFiltrada()
    }

    private fun actualizarListaFiltrada() {
        cochesFiltrados = coches.toMutableList()
    }

    override fun onBindViewHolder(holder: SelectCarViewHolder, position: Int) {
        val coche = cochesFiltrados[position]
        val isSelected = cochesSeleccionados.contains(coche.idCoche)

        // Mostrar datos usando el mismo formato que CarAdapter
        holder.tvDorsal.text = coche.dorsal.toString()
        holder.carName.text = "${coche.marca} ${coche.modelo}"
        holder.teamName.text = holder.itemView.context.getString(
            R.string.label_color_prefix, 
            coche.color
        )

        // Color inicial del status dot
        val status = try {
            CarStatus.valueOf(coche.status)
        } catch (e: Exception) {
            CarStatus.GREEN
        }
        updateStatusColor(holder.statusDot, status)

        // Desactivar click en status dot (solo visual aquí)
        holder.statusDot.isClickable = false
        holder.statusDot.isFocusable = false

        // Ocultar botones de editar/eliminar ya que no los necesitamos aquí
        holder.itemView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnEditCar)?.visibility = View.GONE
        holder.itemView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDeleteCar)?.visibility = View.GONE

        // Cambiar apariencia visual si está seleccionado (igual que SearchCarAdapter)
        if (isSelected) {
            holder.cardView.strokeWidth = 4
            holder.cardView.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.primary_orange)
            holder.cardView.cardElevation = 8f
            holder.carName.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.primary_orange)
            )
            holder.teamName.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
            holder.tvDorsal.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.primary_orange)
            )
        } else {
            holder.cardView.strokeWidth = 0
            holder.cardView.cardElevation = 2f
            holder.carName.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
            holder.teamName.setTextColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)
            )
            holder.tvDorsal.setTextColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.holo_orange_dark)
            )
        }

        holder.itemView.setOnClickListener {
            onCocheClick(coche.idCoche)
        }
    }

    override fun getItemCount(): Int = cochesFiltrados.size

    private fun updateStatusColor(dot: View, status: CarStatus) {
        val colorRes = when (status) {
            CarStatus.GREEN -> R.drawable.bg_status_green
            CarStatus.YELLOW -> R.drawable.bg_status_yellow
            CarStatus.RED -> R.drawable.bg_status_red
        }
        dot.setBackgroundResource(colorRes)
    }

    fun filter(query: String) {
        android.util.Log.d("SelectTournamentCarsAdapter", "Filter llamado con query: '$query', coches.size: ${coches.size}")
        actualizarListaFiltrada()
        if (query.isNotEmpty()) {
            cochesFiltrados = cochesFiltrados.filter {
                it.marca.contains(query, ignoreCase = true) ||
                it.modelo.contains(query, ignoreCase = true) ||
                it.color.contains(query, ignoreCase = true) ||
                it.dorsal.toString().contains(query, ignoreCase = true)
            }.toMutableList()
        }
        android.util.Log.d("SelectTournamentCarsAdapter", "Después de filtrar, cochesFiltrados.size: ${cochesFiltrados.size}")
        notifyDataSetChanged()
    }
}

