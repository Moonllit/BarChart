package com.example.barchartview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val arrayList = listOf(
                BarData("03", 80000f),
                BarData("04", 75000f),
                BarData("05", 65674f),
                BarData("06", 55000f),
                BarData("07", 43000f),
                BarData("08", 38000f),
                BarData("09", 30000f),
                BarData("10", 25000f),
                BarData("11", 15000f),
                BarData("12", 6000f)
        )

        chart.data = arrayList
    }
}
