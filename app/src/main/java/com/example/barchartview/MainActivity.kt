package com.example.barchartview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val arrayList = listOf(
            BarData("01/01/2020", 80000),
            BarData("01/02/2020", 75000),
            BarData("01/03/2020", 65674),
            BarData("01/06/2020", 55000),
            BarData("01/07/2020", 43000),
            BarData("01/05/2020", 38000),
            BarData("01/09/2020", 30000),
            BarData("01/10/2020", 25000),
            BarData("01/11/2020", 15000),
            BarData("01/02/2020", 6000),
            BarData("01/04/2020", 500)
        )

        val creditAmountList = listOf(
            BarData("01", 0),
            BarData("02", 2),
            BarData("03", 3),
            BarData("04", 4),
            BarData("05", 0),
            BarData("06", 2)
        )

        val creditRequestAmountList = listOf(
            BarData("30 дней", 0),
            BarData("60 дней", 1),
            BarData("90 дней", 2),
            BarData("180 дней", 3),
            BarData("1 год", 4)
        )

        //chart.barData = arrayList
        //credit_amount.barData = creditAmountList
        credit_request_amount.barData = creditRequestAmountList
    }
}
