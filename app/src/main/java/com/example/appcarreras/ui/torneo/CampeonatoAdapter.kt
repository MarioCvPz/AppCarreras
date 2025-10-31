package com.example.appcarreras.ui.torneo

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appcarreras.R

class CampeonatoAdapter(
    private val context: Context,
    private val listaCampeonatos: MutableList<Campeonato>
) : RecyclerView.Adapter<CampeonatoAdapter.CampeonatoViewHolder>() {

    private var listaFiltrada = listaCampeonatos.toMutableList()
    inner class CampeonatoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTextView: TextView = itemView.findViewById(R.id.tvNombreCampeonato)
        val detallesTextView: TextView = itemView.findViewById(R.id.tvDetallesCampeonato)
        val iconTrophy: ImageView = itemView.findViewById(R.id.iconTrophy) // 👈 añadimos referencia

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
        holder.iconTrophy.imageTintList = ColorStateList.valueOf(campeonato.colorIcono)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, TorneoDetailActivity::class.java)
            intent.putExtra("nombreCampeonato", campeonato.nombre)
            intent.putExtra("TORNEO_ID", campeonato.idTorneo.toLong())
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listaFiltrada.size

    fun actualizarLista(nuevaLista: List<Campeonato>) {
        listaCampeonatos.clear()
        listaCampeonatos.addAll(nuevaLista)
        listaFiltrada = listaCampeonatos.toMutableList()
        notifyDataSetChanged()
    }

}
