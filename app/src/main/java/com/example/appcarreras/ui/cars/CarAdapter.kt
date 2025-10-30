package com.example.appcarreras.ui.cars

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appcarreras.R
import com.example.appcarreras.data.database.DatabaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CarAdapter(private val context: Context,private val cars: MutableList<Car>) :
    RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    private var filteredList = cars.toMutableList()

    inner class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDorsal: TextView = view.findViewById(R.id.tvDorsal)
        val carName: TextView = view.findViewById(R.id.tvCarName)
        val teamName: TextView = view.findViewById(R.id.tvTeamName)
        val statusDot: View = view.findViewById(R.id.statusDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = filteredList[position]

        // Mostrar dorsal sin '#'
        val dorsal = car.name.substringBefore(" ").replace("#", "")
        holder.tvDorsal.text = dorsal

        // Mostrar datos
        holder.carName.text = car.name.substringAfter(" ")
        holder.teamName.text = car.team

        // Color inicial
        updateStatusColor(holder.statusDot, car.status)

        // Pulsar para cambiar color
        holder.statusDot.setOnClickListener {
            val nextStatus = when (car.status) {
                CarStatus.GREEN -> CarStatus.YELLOW
                CarStatus.YELLOW -> CarStatus.RED
                CarStatus.RED -> CarStatus.GREEN
            }
            car.status = nextStatus
            updateStatusColor(holder.statusDot, nextStatus)

            // 🔥 Actualizar en la base de datos
            val db = DatabaseProvider.getDatabase(context)
            val cocheDao = db.cocheDao()
            CoroutineScope(Dispatchers.IO).launch {
                cocheDao.actualizarStatus(car.id, nextStatus.name)
            }
        }
    }

    override fun getItemCount(): Int = filteredList.size

    private fun updateStatusColor(dot: View, status: CarStatus) {
        val colorRes = when (status) {
            CarStatus.GREEN -> R.drawable.bg_status_green
            CarStatus.YELLOW -> R.drawable.bg_status_yellow
            CarStatus.RED -> R.drawable.bg_status_red
        }
        dot.setBackgroundResource(colorRes)
    }

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
