package com.beacat.calendar.ladycal.di

import android.content.Context
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
}
