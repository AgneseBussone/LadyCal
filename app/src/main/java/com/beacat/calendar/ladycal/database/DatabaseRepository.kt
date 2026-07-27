package com.beacat.calendar.ladycal.database

interface DatabaseRepository {
    suspend fun getPeriods(): List<Period>
    suspend fun addPeriod(period: Period): Long
    suspend fun updatePeriod(period: Period)
    suspend fun deletePeriod(period: Period)
}
