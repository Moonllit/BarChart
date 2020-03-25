package com.example.barchartview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import java.text.DecimalFormat

class BarChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : this(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int, defStyleRes: Int) : this(
        context,
        attrs
    )

    var data: List<BarData> = listOf()
        set(value) {
            field = value
            maxValueOfData = Float.MIN_VALUE
            for (bar in data) {
                if (maxValueOfData < bar.value) maxValueOfData = bar.value
            }
            findMaxWidthOfText()
            invalidate()
        }

    var selectedBarColor: Int = Color.parseColor("#53c283")
        set(value) {
            field = value
            invalidate()
        }

    var barColor: Int = Color.parseColor("#e1e5e7")
        set(value) {
            field = value
            invalidate()
        }

    var outerBarCircleColor: Int = Color.parseColor("#53c283")
        set(value) {
            field = value
            invalidate()
        }

    var innerBarCircleColor: Int = Color.parseColor("#ffffff")
        set(value) {
            field = value
            invalidate()
        }

    var textColor: Int = Color.parseColor("#96a6a7")
        set(value) {
            field = value
            invalidate()
        }

    var selectedBarLabelColor: Int = Color.parseColor("#53c283")
        set(value) {
            field = value
            invalidate()
        }

    var barCornerRadius: Float = 20f
        set(value) {
            field = value
            invalidate()
        }

    var barCircleRadius: Float = 6f
        set(value) {
            field = value
            invalidate()
        }

    var axisStrokeWidth: Float = 3f
        set(value) {
            field = value
            invalidate()
        }

    var axisFontSize: Int = 12
        set(value) {
            field = value
            invalidate()
        }

    var textFontId: Int = R.font.roboto_medium
        set(value) {
            field = value
            invalidate()
        }

    var maxValueCountOnYAxis: Int = 6
        set(value) {
            field = value
            invalidate()
        }

    var maxDataBarCountOnXAxis: Int = 6
        set(value) {
            field = value
            invalidate()
        }

    var marginXAxisAndValue: Float = 10f
        set(value) {
            field = value
            invalidate()
        }

    var marginYAxisAndValue: Float = 35f
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    private var barCornerRadiusInDp = 0f
    private var barCircleRadiusInDp = 0f
    private var marginXAxisAndValueInDp = 0f
    private var marginYAxisAndValueInDp = 0f

    private var maxValueOfData: Float = 0f

    private var maxWidthOfYAxisText = 0
    private var maxHeightOfXAxisText = 0

    private val barFigures = arrayListOf<RectF>()
    private var clicked: Boolean = false
    private var rectIndex = 0

    var lastScrollPos = 0f
    private val gestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                lastScrollPos += distanceX
                scrollX = lastScrollPos.toInt()
                return true
            }
        })
    }

    init {
        marginXAxisAndValueInDp = dpToPixels(context, marginXAxisAndValue)
        marginYAxisAndValueInDp = dpToPixels(context, marginYAxisAndValue)

        barCornerRadiusInDp = dpToPixels(context, barCornerRadius)
        barCircleRadiusInDp = dpToPixels(context, barCircleRadius)

        initPaint()
    }

    private fun initPaint() {
        paint.style = Paint.Style.FILL

        axisPaint.color = textColor
        axisPaint.strokeWidth = axisStrokeWidth

        textPaint.typeface = ResourcesCompat.getFont(context, textFontId)
        textPaint.textSize = dpToPixels(context, axisFontSize.toFloat())
        textPaint.color = textColor
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                rectIndex = getRectIndexFor(x, y)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val finalIndex = getRectIndexFor(x, y)
                if (finalIndex == rectIndex) {
                    onClick(event)
                }
            }
        }
        gestureDetector.onTouchEvent(event)
        return true
    }

    /**
     * Calculate the maximum width occupied by any of given bar chart data. Width is calculated
     * based on font used and its size.
     */
    private fun findMaxWidthOfText() {
        maxWidthOfYAxisText = Int.MIN_VALUE
        maxHeightOfXAxisText = Int.MIN_VALUE

        val bounds = Rect()
        for (bar in data) {
            val currentTextWidth = textPaint.measureText((bar.value).toString())
            if (maxWidthOfYAxisText < currentTextWidth) maxWidthOfYAxisText =
                currentTextWidth.toInt()

            textPaint.getTextBounds(bar.xAxisName, 0, bar.xAxisName.length, bounds)
            if (maxHeightOfXAxisText < bounds.height()) maxHeightOfXAxisText = bounds.height()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defaultSize = 300

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> {
                heightSize
            }
            MeasureSpec.AT_MOST -> {
                defaultSize.coerceAtMost(heightSize)
            }
            else -> defaultSize
        }

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> {
                widthSize
            }
            MeasureSpec.AT_MOST -> {
                defaultSize.coerceAtMost(widthSize)
            }
            else -> defaultSize
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        val usableViewHeight = height - paddingBottom - paddingTop
        val usableViewWidth = width - paddingLeft - paddingRight
        val origin: Point = getOrigin()

        drawAxis(canvas, origin, usableViewWidth)
        if (data.isEmpty()) return

        //draw bar chart
        barFigures.clear()
        drawBarChart(canvas, usableViewHeight, usableViewWidth, origin)
    }

    private fun drawAxis(canvas: Canvas, origin: Point, usableViewWidth: Int) {
        canvas.drawLine(
            origin.x.toFloat(),
            origin.y.toFloat(),
            origin.x + usableViewWidth - (maxWidthOfYAxisText + marginYAxisAndValue),
            origin.y.toFloat(),
            axisPaint
        )
    }

    private fun drawBarChart(
        canvas: Canvas,
        usableViewHeight: Int,
        usableViewWidth: Int,
        origin: Point
    ) {
        // количество столбцов
        val barAndVacantSpaceCount = if (data.size <= maxDataBarCountOnXAxis) {
            (data.size * 2 shl 1)
        } else {
            maxDataBarCountOnXAxis * 2 shl 1
        }
        val widthFactor: Int = (usableViewWidth - maxWidthOfYAxisText) / barAndVacantSpaceCount
        var x1: Int
        var x2: Int
        var y1: Int
        var y2: Int
        val maxValue: Float = maxValueOfData

        for (index in 0 until data.size * 2 - 1) {
            if (index % 2 == 0) { // draw data bar
                x1 = origin.x + ((index shl 1) + 1) * widthFactor
                x2 = origin.x + ((index shl 1) + 2) * widthFactor
                val barHeight =
                    ((usableViewHeight - getXAxisLabelAndMargin()) * data[index shr 1].value / maxValue).toInt()
                y1 = origin.y - barHeight
                y2 = origin.y - marginXAxisAndValueInDp.toInt()

                val rect = RectF(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())
                barFigures.add(rect)

                paint.color = barColor
                canvas.drawRoundRect(rect, barCornerRadiusInDp, barCornerRadiusInDp, paint)

                paint.color = outerBarCircleColor
                canvas.drawCircle(
                    (x1 + x2) / 2f,
                    y1.toFloat() + barCircleRadiusInDp,
                    barCircleRadiusInDp,
                    paint
                )

                paint.color = innerBarCircleColor
                canvas.drawCircle(
                    (x1 + x2) / 2f,
                    y1.toFloat() + barCircleRadiusInDp,
                    barCircleRadiusInDp / 2,
                    paint
                )

                showXAxisLabel(origin, data[index shr 1].xAxisName, x1 + (x2 - x1) / 2, canvas)

            } else { // draw empty bar
                x1 = origin.x + ((index shl 1) + 1) * widthFactor
                x2 = origin.x + ((index shl 1) + 2) * widthFactor
                val barHeight =
                    ((usableViewHeight - getXAxisLabelAndMargin()) * data[index shr 1].value / maxValue).toInt()
                y1 = origin.y - barHeight
                y2 = origin.y - marginXAxisAndValueInDp.toInt()

                val rect = RectF(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())
                paint.color = barColor
                canvas.drawRoundRect(rect, barCornerRadiusInDp, barCornerRadiusInDp, paint)
            }
        }

        if (clicked && rectIndex >= 0) drawHighlightBar(canvas)

        showYAxisLabels(origin, usableViewHeight - getXAxisLabelAndMargin(), canvas)
    }

    private fun drawHighlightBar(canvas: Canvas) {
        log("drawHighlightBar() index: $rectIndex, array size: ${barFigures.size}")
        if (rectIndex == -1) return

        val rect = barFigures[rectIndex]

        paint.color = selectedBarColor
        canvas.drawRoundRect(rect, barCornerRadiusInDp, barCornerRadiusInDp, paint)

        paint.color = innerBarCircleColor
        canvas.drawCircle(
            rect.centerX(),
            rect.top + barCircleRadiusInDp,
            barCircleRadiusInDp / 2,
            paint
        )

        drawHighlightBarLabel(canvas, rect)
    }

    private fun drawHighlightBarLabel(canvas: Canvas, rect: RectF) {
        val halfBar = rect.width() / 2

        path.apply {
            reset()
            moveTo(rect.centerX(), rect.top - 10)
            lineTo(rect.centerX() + halfBar, rect.top - rect.width())
            lineTo(rect.centerX() - halfBar, rect.top - rect.width())
            close()
        }

        paint.color = selectedBarLabelColor
        canvas.drawPath(path, paint)
    }

    private fun getFormattedValue(value: Float): String {
        val precision = DecimalFormat("0.0")
        return precision.format(value.toDouble())
    }

    /**
     * Draws Y axis labels and marker points along Y axis.
     *
     * @param origin           coordinates of origin on canvas
     * @param usableViewHeight view height after removing the padding
     * @param canvas           canvas to draw the chart
     */
    private fun showYAxisLabels(
        origin: Point,
        usableViewHeight: Int,
        canvas: Canvas
    ) {
        val yAxisValueInterval = usableViewHeight / maxValueCountOnYAxis
        val dataInterval: Float = maxValueOfData / maxValueCountOnYAxis
        var valueToBeShown = maxValueOfData

        //draw all texts from top to bottom
        for (index in 0 until maxValueCountOnYAxis) {
            val string = getFormattedValue(valueToBeShown)

            val bounds = Rect()
            textPaint.getTextBounds(string, 0, string.length, bounds)
            val y =
                (origin.y - usableViewHeight) + yAxisValueInterval * index + (bounds.height() shl 1)

            canvas.drawText(
                string,
                origin.x - bounds.width() - marginYAxisAndValueInDp,
                y.toFloat(),
                textPaint
            )
            valueToBeShown -= dataInterval
        }
    }

    /**
     * Draws X axis labels.
     *
     * @param origin  coordinates of origin on canvas
     * @param label   label to be drawn below a bar along X axis
     * @param centerX center x coordinate of the given bar
     * @param canvas  canvas to draw the chart
     */
    private fun showXAxisLabel(
        origin: Point,
        label: String,
        centerX: Int,
        canvas: Canvas
    ) {
        val bounds = Rect()
        textPaint.getTextBounds(label, 0, label.length, bounds)
        val y: Int = origin.y + marginXAxisAndValueInDp.toInt() + maxHeightOfXAxisText
        val x = centerX - bounds.width() / 2

        canvas.drawText(label, x.toFloat(), y.toFloat(), textPaint)
    }

    /**
     * Returns the X axis' maximum label height and margin between label and the X axis.
     *
     * @return the X axis' maximum label height and margin between label and the X axis
     */
    private fun getXAxisLabelAndMargin(): Int {
        return maxHeightOfXAxisText + marginXAxisAndValueInDp.toInt()
    }

    /**
     * Returns the origin coordinates in canvas' coordinates.
     *
     * @return origin's coordinates
     */
    private fun getOrigin(): Point {
        return if (data.isNotEmpty()) {
            Point(
                paddingLeft + maxWidthOfYAxisText + marginYAxisAndValueInDp.toInt(),
                height - paddingBottom - getXAxisLabelAndMargin()
            )
        } else {
            Point(
                paddingLeft + maxWidthOfYAxisText + marginYAxisAndValueInDp.toInt(),
                height - paddingBottom
            )
        }
    }

    private fun getRectIndexFor(x: Float, y: Float): Int {
        barFigures.forEachIndexed { index, rectF ->
            if (rectF.contains(x, y)) return index
        }

        return -1 // x, y do not lie in our view
    }

    private fun onClick(event: MotionEvent) {
        val x = event.x
        val y = event.y
        rectIndex = getRectIndexFor(x, y)
        clicked = true
        invalidate()
    }

    private fun log(message: String) {
        Log.i(this::class.java.simpleName, message)
    }

    private fun dpToPixels(context: Context, dpValue: Float): Float {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, metrics)
    }
}
