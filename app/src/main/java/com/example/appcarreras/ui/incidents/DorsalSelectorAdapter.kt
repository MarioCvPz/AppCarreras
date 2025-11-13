package com.example.appcarreras.ui.incidents

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.appcarreras.R
import com.google.android.material.card.MaterialCardView

class DorsalSelectorAdapter(
    private val dorsales: List<Int>,
    private val onDorsalSelected: (Int) -> Unit
) : RecyclerView.Adapter<DorsalSelectorAdapter.DorsalViewHolder>() {

    private var selectedDorsal: Int? = null

    inner class DorsalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        val tvDorsal: TextView = itemView.findViewById(R.id.tvDorsalNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DorsalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dorsal_selector, parent, false)
        return DorsalViewHolder(view)
    }

    override fun onBindViewHolder(holder: DorsalViewHolder, position: Int) {
        val dorsal = dorsales[position]
        holder.tvDorsal.text = dorsal.toString()

        val isSelected = selectedDorsal == dorsal
        updateCardAppearance(holder.cardView, holder.tvDorsal, isSelected)

        holder.itemView.setOnClickListener {
            val previousSelected = selectedDorsal
            selectedDorsal = dorsal
            onDorsalSelected(dorsal)
            
            // Notificar cambios para actualizar la UI
            if (previousSelected != null) {
                val previousPosition = dorsales.indexOf(previousSelected)
                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition)
                }
            }
            notifyItemChanged(position)
        }
    }

    private fun updateCardAppearance(cardView: MaterialCardView, textView: TextView, isSelected: Boolean) {
        if (isSelected) {
            cardView.strokeColor = ContextCompat.getColor(cardView.context, R.color.primary_orange)
            cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, R.color.primary_orange))
            textView.setTextColor(ContextCompat.getColor(cardView.context, android.R.color.white))
            cardView.alpha = 1.0f
        } else {
            cardView.strokeColor = ContextCompat.getColor(cardView.context, android.R.color.transparent)
            cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, R.color.card_background))
            textView.setTextColor(ContextCompat.getColor(cardView.context, R.color.primary_orange))
            cardView.alpha = 1.0f
        }
    }

    fun setSelectedDorsal(dorsal: Int?) {
        val previousSelected = selectedDorsal
        selectedDorsal = dorsal
        if (previousSelected != null) {
            val previousPosition = dorsales.indexOf(previousSelected)
            if (previousPosition != -1) {
                notifyItemChanged(previousPosition)
            }
        }
        if (dorsal != null) {
            val position = dorsales.indexOf(dorsal)
            if (position != -1) {
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int = dorsales.size
}

