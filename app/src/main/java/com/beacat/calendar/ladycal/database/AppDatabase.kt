package com.beacat.calendar.ladycal.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Period::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun periodDao(): PeriodDao
}
