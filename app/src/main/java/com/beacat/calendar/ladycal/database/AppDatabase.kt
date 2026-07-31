package com.beacat.calendar.ladycal.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [
    Period::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun periodDao(): PeriodDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "ladycal.db")
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
