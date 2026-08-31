package com.era.app.di

import com.era.app.repository.AuthRepository
import com.era.app.repository.RemoteAuthRepository
import com.era.app.repository.RemoteUserRepository
import com.era.app.repository.RoomProgresoRepository
import com.era.app.repository.SesionRepository
import com.era.app.repository.TokenManagerSesion
import com.era.app.repository.UserRepository
import com.era.app.repository.ProgresoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: RemoteAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSesionRepository(impl: TokenManagerSesion): SesionRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: RemoteUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindProgresoRepository(impl: RoomProgresoRepository): ProgresoRepository
}
