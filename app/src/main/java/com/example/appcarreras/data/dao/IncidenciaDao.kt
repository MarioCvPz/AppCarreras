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

    @Query(
        """
        SELECT incidencias.*, coches.dorsal AS dorsal, coches.marca AS marca, coches.modelo AS modelo
        FROM incidencias
        INNER JOIN coches ON coches.idCoche = incidencias.cocheId
        WHERE incidencias.torneoId = :torneoId
        AND incidencias.carreraId = :carreraId
        ORDER BY incidencias.hora DESC, incidencias.minuto DESC
        """
    )
    suspend fun obtenerIncidenciasConCoche(torneoId: Int, carreraId: Int): List<IncidenciaConCoche>
    @Update
    suspend fun actualizarIncidencia(incidencia: IncidenciaEntity)

    @Query("SELECT * FROM incidencias WHERE idIncidencia = :idIncidencia LIMIT 1")
    suspend fun obtenerIncidenciaPorId(idIncidencia: Int): IncidenciaEntity?

    @Delete
    suspend fun eliminarIncidencia(incidencia: IncidenciaEntity)

    data class IncidenciaConCoche(
        @Embedded val incidencia: IncidenciaEntity,
        @ColumnInfo(name = "dorsal") val dorsal: Int,
        @ColumnInfo(name = "marca") val marca: String,
        @ColumnInfo(name = "modelo") val modelo: String,
    )
}
