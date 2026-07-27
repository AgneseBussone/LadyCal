package com.beacat.calendar.ladycal.mainscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beacat.calendar.ladycal.database.DatabaseRepository
import com.beacat.calendar.ladycal.database.Period
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DatabaseRepository,
) : ViewModel() {

    public fun resetCalendarToToday(){

    }

    fun startPeriod(selectedDay: LocalDate?) {
        val zone = ZoneId.systemDefault()
        val day = selectedDay ?: LocalDate.now()
        val start = day.atStartOfDay(zone).toInstant().toEpochMilli()

        // todo: use fix length or average from past cycles
        val end = day.plusDays(7).atTime(23, 59).atZone(zone).toInstant().toEpochMilli()
        viewModelScope.launch {
            repository.addPeriod(
                Period(startDayTimestamp = start.toString(), endDayTimestamp = end.toString()),
            )
        }
    }

    public fun addMedication() {

    }
}
