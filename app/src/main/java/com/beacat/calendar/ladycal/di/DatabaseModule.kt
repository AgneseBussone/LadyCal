package com.beacat.calendar.ladycal.di

import android.content.Context
import com.beacat.calendar.ladycal.database.AppDatabase
import com.beacat.calendar.ladycal.database.PeriodDao
import com.tyczj.extendedcalendarview.PeriodDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePeriodDatabase(@ApplicationContext context: Context): PeriodDatabase =
        PeriodDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.build(context)

    @Provides
    fun providePeriodDao(db: AppDatabase): PeriodDao = db.periodDao()
}
