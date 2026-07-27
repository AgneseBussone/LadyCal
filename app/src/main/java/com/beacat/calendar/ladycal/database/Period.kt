package com.beacat.calendar.ladycal.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "period")
data class Period(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDayTimestamp: String,
    val endDayTimestamp: String,
)
