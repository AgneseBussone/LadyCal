package com.beacat.calendar.ladycal.database

import java.time.YearMonth

interface DatabaseRepository {
    suspend fun getPeriods(): List<Period>
    suspend fun getPeriodsForMonth(month: YearMonth): List<Period>
    suspend fun addPeriod(period: Period): Long
    suspend fun updatePeriod(period: Period)
    suspend fun deletePeriod(period: Period)
}
