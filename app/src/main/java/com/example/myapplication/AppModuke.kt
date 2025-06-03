package com.example.myapplication

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class Dispatcher(val dispatcher: CoroutinesDispatcherType)

enum class CoroutinesDispatcherType {
    IO, DEFAULT, MAIN
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserService(): UserService = ServiceFactory.createUserService()

    @Provides
    @Dispatcher(CoroutinesDispatcherType.IO)
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(CoroutinesDispatcherType.DEFAULT)
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Dispatcher(CoroutinesDispatcherType.MAIN)
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}