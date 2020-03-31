package com.example.barchartview

import java.text.SimpleDateFormat
import java.util.*

data class BarData(
    val xAxisValue: String,
    val barValue: Int
) {

    fun isCurrentMonth(): Boolean {
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(xAxisValue)
        val calendar = Calendar.getInstance()
        calendar.time = date
        val now = Calendar.getInstance()

        return calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH)
    }

    fun getBarColor(): String {
        return when (barValue) {
            0 -> "#e1e5e7"
            1 -> "#0097ce"
            2 -> "#53c283"
            3 -> "#f0c300"
            else -> "#d14d57"
        }
    }

    fun getDatePair(): Pair<String, String> = xAxisValue.partition {
        it.isDigit()
    }
}
