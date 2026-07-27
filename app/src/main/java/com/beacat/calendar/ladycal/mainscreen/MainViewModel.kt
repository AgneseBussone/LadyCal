package com.beacat.calendar.ladycal.mainscreen

import androidx.lifecycle.ViewModel
import com.beacat.calendar.ladycal.database.DatabaseRepository
import com.tyczj.extendedcalendarview.PeriodDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DatabaseRepository,
) : ViewModel() {

    public fun resetCalendarToToday(){

    }

    public fun startPeriod() {

    }

    public fun addMedication() {

    }
}
