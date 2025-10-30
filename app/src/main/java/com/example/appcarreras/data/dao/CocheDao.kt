package com.example.appcarreras.data.dao

import androidx.room.*
import com.example.appcarreras.data.entity.CocheEntity

@Dao
interface CocheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCoche(coche: CocheEntity): Long

    @Query("SELECT * FROM coches WHERE torneoId = :torneoId")
    suspend fun obtenerCochesPorTorneo(torneoId: Int): List<CocheEntity>

    @Delete
    suspend fun eliminarCoche(coche: CocheEntity)
}
