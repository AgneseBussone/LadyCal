package com.beacat.calendar.ladycal.database

import javax.inject.Inject

class DatabaseRepositoryImpl @Inject constructor(
    private val periodDao: PeriodDao,
) : DatabaseRepository {

    override suspend fun getPeriods(): List<Period> = periodDao.getAll()

    override suspend fun addPeriod(period: Period): Long = periodDao.insert(period)

    override suspend fun updatePeriod(period: Period) = periodDao.update(period)

    override suspend fun deletePeriod(period: Period) = periodDao.delete(period)
}
