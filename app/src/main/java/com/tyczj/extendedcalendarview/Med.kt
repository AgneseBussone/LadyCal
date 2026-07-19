package com.tyczj.extendedcalendarview

/**
 * Class used to carry the information needed by the medicine chart.
 */

class Med(val date: Long, period_length: Int) {
    var totalQuantity: Int = 0          // total of meds taken
        private set
    val meds = FloatArray(period_length) // how many meds taken per day meds[0] = 5 -> first day, 5 meds

    fun setDay(day: Int, quantity: Int) {
        if (day >= 0 && day < meds.size) {
            meds[day] = quantity.toFloat()
            this.totalQuantity += quantity
        }
    }

    fun getMedsinDay(day: Int): Float {
        if (day >= 0 && day < meds.size) {
            return meds[day]
        }
        return 0f
    }
}
