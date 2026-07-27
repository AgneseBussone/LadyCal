package com.beacat.calendar.ladycal.mainscreen

import androidx.lifecycle.ViewModel
import com.tyczj.extendedcalendarview.PeriodDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val db: PeriodDatabase,
) : ViewModel() {

    val periodLength: Int get() = db.periodLength

    public fun resetCalendarToToday(){

    }

    public fun startPeriod() {

    }

    public fun addMedication() {

    }
}
