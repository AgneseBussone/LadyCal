package com.beacat.calendar.ladycal.mainscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beacat.calendar.ladycal.database.DatabaseRepository
import com.beacat.calendar.ladycal.database.Period
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DatabaseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainViewUIState())
    val uiState: StateFlow<MainViewUIState> = _uiState.asStateFlow()

    init {
        loadMonth()
    }

    private fun loadMonth(month: YearMonth = YearMonth.now()) {
        viewModelScope.launch {
            val periodDays = repository.getPeriodsForMonth(month)
                .flatMap { it.daysInMonth(month) }
            _uiState.update { it.copy(periodDays = periodDays) }
        }
    }

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
                Period(startDayTimestamp = start, endDayTimestamp = end),
            )
        }
    }

    public fun addMedication() {

    }
}


private fun Period.daysInMonth(month: YearMonth): List<LocalDate> {
    val zone = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(startDayTimestamp).atZone(zone).toLocalDate()
    val end = Instant.ofEpochMilli(endDayTimestamp).atZone(zone).toLocalDate()
    return generateSequence(start) { if (it < end) it.plusDays(1) else null }
        .filter { YearMonth.from(it) == month }
        .toList()
}

data class MainViewUIState(
    val today: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val periodDays: List<LocalDate> = listOf(),
    val estimatedDays: List<LocalDate> = listOf()
)