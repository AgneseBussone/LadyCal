package com.beacat.calendar.ladycal.di

import com.beacat.calendar.ladycal.database.DatabaseRepository
import com.beacat.calendar.ladycal.database.DatabaseRepositoryImpl
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
    abstract fun bindDatabaseRepository(impl: DatabaseRepositoryImpl): DatabaseRepository
}
