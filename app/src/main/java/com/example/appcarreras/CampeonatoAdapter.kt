package com.example.appcarreras

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CampeonatoAdapter(
    private val context: Context,
    private var listaCampeonatos: MutableList<Campeonato>
) : RecyclerView.Adapter<CampeonatoAdapter.CampeonatoViewHolder>() {

    private var listaFiltrada = listaCampeonatos.toMutableList()

    inner class CampeonatoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTextView: TextView = itemView.findViewById(R.id.tvNombreCampeonato)
        val detallesTextView: TextView = itemView.findViewById(R.id.tvDetallesCampeonato)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CampeonatoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_campeonato, parent, false)
        return CampeonatoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CampeonatoViewHolder, position: Int) {
        val campeonato = listaFiltrada[position]
        holder.nombreTextView.text = campeonato.nombre
        holder.detallesTextView.text = "Coches: ${campeonato.numCoches}"

        holder.itemView.setOnClickListener {
            val intent = Intent(context, TorneoDetailActivity::class.java)
            intent.putExtra("nombreCampeonato", campeonato.nombre)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listaFiltrada.size

    fun agregarCampeonato(campeonato: Campeonato) {
        listaCampeonatos.add(campeonato)
        listaFiltrada = listaCampeonatos.toMutableList()
        notifyItemInserted(listaFiltrada.size - 1)
    }

    fun filter(query: String) {
        listaFiltrada = if (query.isEmpty()) {
            listaCampeonatos.toMutableList()
        } else {
            listaCampeonatos.filter {
                it.nombre.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
}
