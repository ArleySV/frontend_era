package com.era.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.era.app.data.EraDatabase
import com.era.app.data.dao.NivelDao
import com.era.app.data.dao.ProgresoDao
import com.era.app.data.entity.NivelEntity
import com.era.app.data.entity.NivelJsonDto
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        json: Json,
        nivelDaoProvider: Provider<NivelDao>
    ): EraDatabase {
        return Room.databaseBuilder(
            context,
            EraDatabase::class.java,
            "era_database"
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                scope.launch {
                    try {
                        val catalogJson = context.assets.open("trivia_catalog.json")
                            .bufferedReader().use { it.readText() }
                        val nivelesDto = json.decodeFromString<List<NivelJsonDto>>(catalogJson)
                        
                        val nivelesEntity = nivelesDto.map { dto ->
                            NivelEntity(
                                orden = dto.orden,
                                pregunta = dto.pregunta,
                                opcionA = dto.opciones.getOrElse(0) { "" },
                                opcionB = dto.opciones.getOrElse(1) { "" },
                                opcionC = dto.opciones.getOrElse(2) { "" },
                                respuestaCorrecta = dto.respuestaCorrecta
                            )
                        }
                        
                        nivelDaoProvider.get().insertarTodos(nivelesEntity)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }).build()
    }

    @Provides
    fun provideNivelDao(db: EraDatabase): NivelDao = db.nivelDao()

    @Provides
    fun provideProgresoDao(db: EraDatabase): ProgresoDao = db.progresoDao()
}
