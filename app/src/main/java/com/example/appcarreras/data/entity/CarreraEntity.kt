package com.example.appcarreras.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "carreras",
    foreignKeys = [
        ForeignKey(
            entity = TorneoEntity::class,
            parentColumns = ["idTorneo"],
            childColumns = ["torneoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CarreraEntity(
    @PrimaryKey(autoGenerate = true) val idCarrera: Int = 0,
    val torneoId: Int,
    val nombreCarrera: String
)
