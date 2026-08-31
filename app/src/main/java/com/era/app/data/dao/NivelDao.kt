package com.era.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.era.app.data.entity.NivelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NivelDao {
    @Query("SELECT * FROM niveles ORDER BY orden ASC")
    fun obtenerTodos(): Flow<List<NivelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(niveles: List<NivelEntity>)

    @Query("SELECT COUNT(*) FROM niveles")
    suspend fun contarNiveles(): Int
}
