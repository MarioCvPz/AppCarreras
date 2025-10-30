package com.example.appcarreras.ui.races

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appcarreras.R

class RaceAdapter(
    private val races: List<Race>,
    private val onRaceClick: (Race) -> Unit,
) :
    RecyclerView.Adapter<RaceAdapter.RaceViewHolder>() {

    inner class RaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val raceName: TextView = view.findViewById(R.id.tvRaceName)
        val raceDate: TextView = view.findViewById(R.id.tvRaceDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_race, parent, false)
        return RaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: RaceViewHolder, position: Int) {
        val race = races[position]
        holder.raceName.text = race.name
        holder.raceDate.text = race.date
        holder.itemView.setOnClickListener { onRaceClick(race) }
    }

    override fun getItemCount(): Int = races.size
}
