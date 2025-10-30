package com.example.appcarreras.data.dao

import androidx.room.*
import com.example.appcarreras.data.entity.CarreraEntity

@Dao
interface CarreraDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCarrera(carrera: CarreraEntity): Long

    @Query("SELECT * FROM carreras WHERE torneoId = :torneoId")
    suspend fun obtenerCarrerasPorTorneo(torneoId: Int): List<CarreraEntity>

    @Delete
    suspend fun eliminarCarrera(carrera: CarreraEntity)
}
