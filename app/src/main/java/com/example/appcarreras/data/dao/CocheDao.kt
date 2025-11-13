package com.example.appcarreras.data.dao

import androidx.room.*
import com.example.appcarreras.data.entity.CocheEntity

@Dao
interface CocheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCoche(coche: CocheEntity): Long

    @Query("SELECT * FROM coches WHERE torneoId = :torneoId AND carreraId IS NULL")
    suspend fun obtenerCochesPorTorneo(torneoId: Int): List<CocheEntity>

    @Query("SELECT COUNT(*) FROM coches WHERE torneoId = :torneoId AND carreraId IS NULL")
    suspend fun contarCochesPorTorneo(torneoId: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM coches WHERE torneoId = :torneoId AND carreraId IS NULL AND dorsal = :dorsal)")
    suspend fun existeDorsalEnTorneo(torneoId: Long, dorsal: Int): Boolean

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
}