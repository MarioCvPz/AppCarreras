package com.example.appcarreras.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "incidencias",
    foreignKeys = [
        ForeignKey(
            entity = TorneoEntity::class,
            parentColumns = ["idTorneo"],
            childColumns = ["torneoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CarreraEntity::class,
            parentColumns = ["idCarrera"],
            childColumns = ["carreraId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CocheEntity::class,
            parentColumns = ["idCoche"],
            childColumns = ["cocheId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class IncidenciaEntity(
    @PrimaryKey(autoGenerate = true) val idIncidencia: Int = 0,
    val torneoId: Int,
    val carreraId: Int,
    val cocheId: Int,
    val tipoIncidencia: String,
    val hora: Int,
    val minuto: Int,
    val vueltasPenalizacion: Int
)
