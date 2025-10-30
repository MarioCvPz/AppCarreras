package com.example.appcarreras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RaceAdapter(private val races: List<Race>) :
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
    }

    override fun getItemCount(): Int = races.size
}
