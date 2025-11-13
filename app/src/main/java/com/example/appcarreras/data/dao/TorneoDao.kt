package com.example.appcarreras.data.dao

import androidx.room.*
import com.example.appcarreras.data.entity.TorneoEntity

@Dao
interface TorneoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTorneo(torneo: TorneoEntity): Long

    @Query("SELECT * FROM torneos ORDER BY idTorneo DESC")
    suspend fun obtenerTorneos(): List<TorneoEntity>

    @Delete
    suspend fun eliminarTorneo(torneo: TorneoEntity)

    @Query("SELECT * FROM torneos WHERE nombre LIKE '%' || :nombre || '%' ORDER BY idTorneo DESC")
    suspend fun buscarTorneosPorNombre(nombre: String): List<TorneoEntity>

    @Update
    suspend fun actualizarTorneo(torneo: TorneoEntity)

    @Query("SELECT * FROM torneos WHERE idTorneo = :idTorneo LIMIT 1")
    suspend fun obtenerTorneoPorId(idTorneo: Int): TorneoEntity?

}
