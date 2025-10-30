package com.example.appcarreras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CarAdapter(private val cars: MutableList<Car>) :
    RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    private var filteredList = cars.toMutableList()

    inner class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val carName: TextView = view.findViewById(R.id.tvCarName)
        val teamName: TextView = view.findViewById(R.id.tvTeamName)
        val statusDot: View = view.findViewById(R.id.statusDot)
        val iconCar: ImageView = view.findViewById(R.id.iconCar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = filteredList[position]
        holder.carName.text = car.name
        holder.teamName.text = car.team

        val colorRes = when (car.status) {
            CarStatus.GREEN -> R.drawable.bg_status_green
            CarStatus.YELLOW -> R.drawable.bg_status_yellow
            CarStatus.RED -> R.drawable.bg_status_red
        }
        holder.statusDot.setBackgroundResource(colorRes)
    }

    override fun getItemCount(): Int = filteredList.size

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            cars.toMutableList()
        } else {
            cars.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.team.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
}
