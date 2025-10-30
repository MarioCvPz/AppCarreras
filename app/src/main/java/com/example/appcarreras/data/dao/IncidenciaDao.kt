package com.example.appcarreras.data.dao

import androidx.room.*
import com.example.appcarreras.data.entity.IncidenciaEntity

@Dao
interface IncidenciaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarIncidencia(incidencia: IncidenciaEntity): Long

    @Query("""
        SELECT * FROM incidencias 
        WHERE torneoId = :torneoId 
        AND carreraId = :carreraId
    """)
    suspend fun obtenerIncidenciasPorCarrera(torneoId: Int, carreraId: Int): List<IncidenciaEntity>

    @Delete
    suspend fun eliminarIncidencia(incidencia: IncidenciaEntity)
}
