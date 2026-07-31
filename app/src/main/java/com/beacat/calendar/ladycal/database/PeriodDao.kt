package com.beacat.calendar.ladycal.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PeriodDao {

    @Query("SELECT * FROM period")
    suspend fun getAll(): List<Period>

    @Query(
        "SELECT * FROM period WHERE startDayTimestamp <= :monthEnd " +
            "AND endDayTimestamp >= :monthStart",
    )
    suspend fun getForRange(monthStart: Long, monthEnd: Long): List<Period>

    @Insert
    suspend fun insert(period: Period): Long

    @Update
    suspend fun update(period: Period)

    @Delete
    suspend fun delete(period: Period)
}
