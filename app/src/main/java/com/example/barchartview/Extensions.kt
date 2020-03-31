package com.example.barchartview

import android.content.Context
import android.graphics.Canvas
import android.text.StaticLayout
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.core.graphics.withTranslation

fun dpToPixels(context: Context, dpValue: Float): Float {
    val metrics = context.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, metrics)
}

fun pixelsToDp(context: Context, px: Float): Float {
    return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun StaticLayout.draw(canvas: Canvas, x: Float, y: Float) {
    canvas.withTranslation(x, y) {
        draw(this)
    }
}
