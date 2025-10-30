package com.example.appcarreras.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "coches",
    foreignKeys = [
        ForeignKey(
            entity = TorneoEntity::class,
            parentColumns = ["idTorneo"],
            childColumns = ["torneoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CocheEntity(
    @PrimaryKey(autoGenerate = true) val idCoche: Int = 0,
    val torneoId: Int,
    val marca: String,
    val modelo: String,
    val color: String,
    val dorsal: String
)
