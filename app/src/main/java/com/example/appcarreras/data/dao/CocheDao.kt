package com.example.appcarreras.data.dao

import androidx.room.*
import com.example.appcarreras.data.entity.CocheEntity

@Dao
interface CocheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCoche(coche: CocheEntity): Long

    @Query("SELECT * FROM coches WHERE torneoId = :torneoId AND carreraId IS NULL")
    suspend fun obtenerCochesPorTorneo(torneoId: Int): List<CocheEntity>

    @Query("SELECT * FROM coches WHERE torneoId = :torneoId")
    suspend fun obtenerTodosLosCochesDelTorneo(torneoId: Int): List<CocheEntity>

    @Query("SELECT COUNT(*) FROM coches WHERE torneoId = :torneoId AND carreraId IS NULL")
    suspend fun contarCochesPorTorneo(torneoId: Long): Int

    @Query("SELECT COUNT(*) FROM coches WHERE torneoId = :torneoId")
    suspend fun contarTodosLosCochesDelTorneo(torneoId: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM coches WHERE torneoId = :torneoId AND carreraId IS NULL AND dorsal = :dorsal)")
    suspend fun existeDorsalEnTorneo(torneoId: Long, dorsal: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM coches WHERE torneoId = :torneoId AND dorsal = :dorsal)")
    suspend fun existeDorsalEnTodoElTorneo(torneoId: Long, dorsal: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM coches WHERE torneoId = :torneoId AND dorsal = :dorsal AND idCoche != :excluirCocheId)")
    suspend fun existeDorsalEnTodoElTorneoExcluyendo(torneoId: Long, dorsal: Int, excluirCocheId: Int): Boolean

    @Query("SELECT * FROM coches WHERE torneoId = :torneoId AND carreraId IS NULL AND dorsal = :dorsal LIMIT 1")
    suspend fun obtenerCochePorDorsal(torneoId: Long, dorsal: Int): CocheEntity?

    @Query("SELECT * FROM coches WHERE carreraId = :carreraId")
    suspend fun obtenerCochesPorCarrera(carreraId: Int): List<CocheEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM coches WHERE carreraId = :carreraId AND dorsal = :dorsal)")
    suspend fun existeDorsalEnCarrera(carreraId: Int, dorsal: Int): Boolean

    @Query("SELECT * FROM coches WHERE carreraId = :carreraId AND dorsal = :dorsal LIMIT 1")
    suspend fun obtenerCochePorDorsalEnCarrera(carreraId: Int, dorsal: Int): CocheEntity?

    @Query("UPDATE coches SET status = :nuevoStatus WHERE idCoche = :idCoche")
    suspend fun actualizarStatus(idCoche: Int, nuevoStatus: String)

    @Update
    suspend fun actualizarCoche(coche: CocheEntity)

    @Query("SELECT * FROM coches WHERE idCoche = :idCoche LIMIT 1")
    suspend fun obtenerCochePorId(idCoche: Int): CocheEntity?

    @Delete
    suspend fun eliminarCoche(coche: CocheEntity)

    // Obtener todos los coches de todos los torneos
    @Query("SELECT * FROM coches")
    suspend fun obtenerTodosLosCoches(): List<CocheEntity>

    // Buscar coches excluyendo el torneo actual (si torneoIdExcluir es -1, devuelve todos)
    @Query("SELECT * FROM coches WHERE torneoId != :torneoIdExcluir")
    suspend fun buscarCochesExcluyendoTorneo(torneoIdExcluir: Long): List<CocheEntity>

    // Buscar coches por texto (marca, modelo, color, dorsal) excluyendo el torneo actual
    @Query("""
        SELECT * FROM coches 
        WHERE torneoId != :torneoIdExcluir
        AND (
            marca LIKE '%' || :query || '%' 
            OR modelo LIKE '%' || :query || '%' 
            OR color LIKE '%' || :query || '%' 
            OR CAST(dorsal AS TEXT) LIKE '%' || :query || '%'
        )
        ORDER BY marca, modelo, dorsal
    """)
    suspend fun buscarCochesPorTexto(torneoIdExcluir: Long, query: String): List<CocheEntity>

    data class CocheBusqueda(
        val marca: String,
        val modelo: String,
        val color: String,
        val dorsal: Int
    ) {
        companion object {
            fun fromEntity(entity: CocheEntity): CocheBusqueda {
                return CocheBusqueda(
                    marca = entity.marca,
                    modelo = entity.modelo,
                    color = entity.color,
                    dorsal = entity.dorsal
                )
            }
        }
    }
}