package com.saniblue.app.di

import com.saniblue.app.data.repository.EnsaioRepositoryImpl
import com.saniblue.app.data.repository.HidrometroRepositoryImpl
import com.saniblue.app.data.repository.UsuarioRepositoryImpl
import com.saniblue.app.domain.repository.EnsaioRepository
import com.saniblue.app.domain.repository.HidrometroRepository
import com.saniblue.app.domain.repository.UsuarioRepository
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
    abstract fun bindEnsaioRepository(impl: EnsaioRepositoryImpl): EnsaioRepository

    @Binds
    @Singleton
    abstract fun bindHidrometroRepository(impl: HidrometroRepositoryImpl): HidrometroRepository

    @Binds
    @Singleton
    abstract fun bindUsuarioRepository(impl: UsuarioRepositoryImpl): UsuarioRepository
}
