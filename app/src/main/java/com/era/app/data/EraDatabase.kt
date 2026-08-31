package com.era.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.era.app.data.dao.NivelDao
import com.era.app.data.dao.ProgresoDao
import com.era.app.data.entity.NivelEntity
import com.era.app.data.entity.ProgresoNivelEntity

@Database(
    entities = [NivelEntity::class, ProgresoNivelEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EraDatabase : RoomDatabase() {
    abstract fun nivelDao(): NivelDao
    abstract fun progresoDao(): ProgresoDao
}
