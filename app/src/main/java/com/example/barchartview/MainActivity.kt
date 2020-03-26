package com.example.barchartview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val arrayList = listOf(
            BarData("01/01/2020", 80000f),
            BarData("01/02/2020", 75000f),
            BarData("01/03/2020", 65674f),
            BarData("01/06/2020", 55000f),
            BarData("01/07/2020", 43000f),
            BarData("01/05/2020", 38000f),
            BarData("01/09/2020", 30000f),
            BarData("01/10/2020", 25000f),
            BarData("01/11/2020", 15000f),
            BarData("01/02/2020", 6000f),
            BarData("01/04/2020", 500f)
        )

        chart.barData = arrayList
    }
}
