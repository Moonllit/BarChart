package com.example.barchartview

import java.text.SimpleDateFormat
import java.util.*

data class BarData(
    val xAxisName: String,
    val value: Float
) {

    fun isCurrentMonth(): Boolean {
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(xAxisName)
        val calendar = Calendar.getInstance()
        calendar.time = date
        val now = Calendar.getInstance()

        return calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH)
    }
}
