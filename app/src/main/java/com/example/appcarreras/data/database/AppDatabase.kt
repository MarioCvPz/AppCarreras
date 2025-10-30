package com.example.appcarreras.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.appcarreras.data.dao.*
import com.example.appcarreras.data.entity.*

@Database(
    entities = [
        TorneoEntity::class,
        CocheEntity::class,
        CarreraEntity::class,
        IncidenciaEntity::class
    ],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun torneoDao(): TorneoDao
    abstract fun cocheDao(): CocheDao
    abstract fun carreraDao(): CarreraDao
    abstract fun incidenciaDao(): IncidenciaDao
}
