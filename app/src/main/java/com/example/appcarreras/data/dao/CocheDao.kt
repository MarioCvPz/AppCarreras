package com.example.appcarreras.data.dao

import androidx.room.*
import com.example.appcarreras.data.entity.CocheEntity

@Dao
interface CocheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCoche(coche: CocheEntity): Long

    @Query("SELECT * FROM coches WHERE torneoId = :torneoId")
    suspend fun obtenerCochesPorTorneo(torneoId: Int): List<CocheEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM coches WHERE torneoId = :torneoId AND dorsal = :dorsal)")
    suspend fun existeDorsalEnTorneo(torneoId: Long, dorsal: Int): Boolean

    @Query("UPDATE coches SET status = :nuevoStatus WHERE idCoche = :idCoche")
    suspend fun actualizarStatus(idCoche: Int, nuevoStatus: String)


    @Delete
    suspend fun eliminarCoche(coche: CocheEntity)
}
