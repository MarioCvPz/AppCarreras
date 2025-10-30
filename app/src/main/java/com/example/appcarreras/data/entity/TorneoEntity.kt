package com.example.appcarreras.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "torneos")
data class TorneoEntity(
    @PrimaryKey(autoGenerate = true) val idTorneo: Int = 0,
    val nombre: String

)
