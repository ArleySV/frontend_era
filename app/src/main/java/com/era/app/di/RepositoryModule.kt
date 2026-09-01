package com.era.app.di

import com.era.app.repository.AuthRepository
import com.era.app.repository.AvatarRepository
import com.era.app.repository.LocalFaqRepository
import com.era.app.repository.RemoteAuthRepository
import com.era.app.repository.RemoteAvatarRepository
import com.era.app.repository.RemoteUserRepository
import com.era.app.repository.RemoteFeedbackRepository
import com.era.app.repository.RoomProgresoRepository
import com.era.app.repository.SesionRepository
import com.era.app.repository.TokenManagerSesion
import com.era.app.repository.UserRepository
import com.era.app.repository.ProgresoRepository
import com.era.app.repository.FaqRepository
import com.era.app.repository.FeedbackRepository
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

    @Binds
    @Singleton
    abstract fun bindFaqRepository(impl: LocalFaqRepository): FaqRepository

    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(impl: RemoteFeedbackRepository): FeedbackRepository

    @Binds
    @Singleton
    abstract fun bindAvatarRepository(impl: RemoteAvatarRepository): AvatarRepository
}
