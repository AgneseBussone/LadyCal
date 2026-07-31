package com.beacat.calendar.ladycal.database

import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

class DatabaseRepositoryImpl @Inject constructor(
    private val periodDao: PeriodDao,
) : DatabaseRepository {

    override suspend fun getPeriods(): List<Period> = periodDao.getAll()

    override suspend fun getPeriodsForMonth(month: YearMonth): List<Period> {
        val zone = ZoneId.systemDefault()
        val monthStart = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val monthEnd = month.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        return periodDao.getForRange(monthStart, monthEnd)
    }

    override suspend fun addPeriod(period: Period): Long = periodDao.insert(period)

    override suspend fun updatePeriod(period: Period) = periodDao.update(period)

    override suspend fun deletePeriod(period: Period) = periodDao.delete(period)
}
