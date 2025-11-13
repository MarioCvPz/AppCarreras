package com.example.appcarreras.ui.racedetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appcarreras.R

class RaceIncidentAdapter(
    private val incidents: MutableList<RaceIncident>,
    private val onEditClick: (RaceIncident) -> Unit,
    private val onDeleteClick: (RaceIncident) -> Unit
) : RecyclerView.Adapter<RaceIncidentAdapter.RaceIncidentViewHolder>() {

    inner class RaceIncidentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val carNumber: TextView = view.findViewById(R.id.tvIncidentCarNumber)
        val carName: TextView = view.findViewById(R.id.tvIncidentCarName)
        val incidentType: TextView = view.findViewById(R.id.tvIncidentType)
        val incidentTime: TextView = view.findViewById(R.id.tvIncidentTime)
        val penaltyLaps: TextView = view.findViewById(R.id.tvIncidentPenalty)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditIncident)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteIncident)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RaceIncidentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_race_incident, parent, false)
        return RaceIncidentViewHolder(view)
    }

    override fun onBindViewHolder(holder: RaceIncidentViewHolder, position: Int) {
        val incident = incidents[position]
        holder.carNumber.text = incident.carNumber.toString()
        holder.carName.text = incident.carName
        holder.incidentType.text = incident.incidentType
        holder.incidentTime.text = incident.time
        holder.penaltyLaps.text = holder.itemView.context.getString(
            R.string.label_penalty_laps_value,
            incident.penaltyLaps
        )
        holder.btnEdit.setOnClickListener { onEditClick(incident) }
        holder.btnDelete.setOnClickListener { onDeleteClick(incident) }
    }

    override fun getItemCount(): Int = incidents.size

    fun updateData(newData: List<RaceIncident>) {
        incidents.clear()
        incidents.addAll(newData)
        notifyDataSetChanged()
    }
}