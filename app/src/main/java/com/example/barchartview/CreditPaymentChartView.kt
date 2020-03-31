package com.example.barchartview

import android.content.Context
import android.graphics.*
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import kotlin.math.sqrt

class CreditPaymentChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : this(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int, defStyleRes: Int) : this(
        context,
        attrs
    )

    var barData: List<BarData> = listOf()
        set(value) {
            field = value
            maxValueOfData = Int.MIN_VALUE
            for (bar in barData) {
                if (maxValueOfData < bar.barValue) maxValueOfData = bar.barValue
            }
            findMaxWidthOfText()
            invalidate()
        }

    var selectedBarColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var barColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var outerBarCircleColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var innerBarCircleColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var textColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var selectedBarLabelColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var selectedBarTextLabelColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var barCornerRadius: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    var axisStrokeWidth: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    var fontSize: Int = 12
        set(value) {
            field = value
            invalidate()
        }

    var textFontId: Int = R.font.roboto_regular
        set(value) {
            field = value
            invalidate()
        }

    var textLabelFontId: Int = R.font.roboto_medium
        set(value) {
            field = value
            invalidate()
        }

    var maxValueCountOnYAxis: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var maxVisibleBarCount: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    private var marginXAxisAndValueInDp = 0f
    private var marginYAxisAndValueInDp = 0f

    private var maxValueOfData: Int = 0

    private var maxWidthOfYAxisText = 0
    private var maxHeightOfXAxisText = 0

    private val barRects = arrayListOf<RectF>()
    private var isBarClicked: Boolean = false
    private var clickedBarRectIndex = -1

    private var barAndVacantSpaceCount = 0
    private var barWidth = 0
    private var defaultBarWidth = 0
    private var minBarHeight = 0
    private var barAxisWidth = 0

    private var isFirstLaunch = true
    private var currentMonthBarIndex: Int = -1


    private var axisStartX = 0
    private var tapDownX = 0f
    private var tapDownY = 0f
    private var lastScrollPos = 0f
    private val gestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                tapDownX = e.x
                tapDownY = e.y
                return true
            }

            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (barData.size <= maxVisibleBarCount) return true

                if (lastScrollPos + width >= barAxisWidth) {
                    if (e1.x < e2.x) {
                        lastScrollPos += distanceX
                        axisStartX += distanceX.toInt()
                        scrollX = lastScrollPos.toInt()
                    }
                } else if (lastScrollPos <= 0) {
                    axisStartX = paddingLeft + maxWidthOfYAxisText + marginYAxisAndValueInDp.toInt()
                    if (e1.x > e2.x) {
                        lastScrollPos += distanceX
                        axisStartX += distanceX.toInt()
                        scrollX = lastScrollPos.toInt()
                    }
                } else {
                    lastScrollPos += distanceX
                    axisStartX += distanceX.toInt()
                    scrollX = lastScrollPos.toInt()
                }

                return true
            }
        })
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CreditPaymentChartView,
            0, 0
        ).apply {
            try {
                selectedBarColor =
                    getColor(
                        R.styleable.CreditPaymentChartView_selectedBarColor,
                        Color.parseColor("#53c283")
                    )
                barColor = getColor(
                    R.styleable.CreditPaymentChartView_barColor,
                    Color.parseColor("#e1e5e7")
                )
                outerBarCircleColor = getColor(
                    R.styleable.CreditPaymentChartView_outerBarCircleColor,
                    Color.parseColor("#53c283")
                )
                innerBarCircleColor =
                    getColor(
                        R.styleable.CreditPaymentChartView_innerBarCircleColor,
                        Color.parseColor("#ffffff")
                    )
                textColor =
                    getColor(
                        R.styleable.CreditPaymentChartView_textColor,
                        Color.parseColor("#96a6a7")
                    )
                selectedBarLabelColor =
                    getColor(
                        R.styleable.CreditPaymentChartView_selectedBarLabelColor,
                        Color.parseColor("#f6f8f9")
                    )
                selectedBarTextLabelColor =
                    getColor(
                        R.styleable.CreditPaymentChartView_selectedBarTextLabelColor,
                        Color.parseColor("#2c3e50")
                    )
                barCornerRadius =
                    getDimension(R.styleable.CreditPaymentChartView_barCornerRadius, 20F)
                axisStrokeWidth =
                    getDimension(R.styleable.CreditPaymentChartView_axisStrokeWidth, 3F)
                maxValueCountOnYAxis =
                    getInteger(R.styleable.CreditPaymentChartView_maxValueCountOnYAxis, 6)
                maxVisibleBarCount =
                    getInteger(R.styleable.CreditPaymentChartView_maxVisibleBarCount, 6)
            } finally {
                recycle()
            }
        }

        marginXAxisAndValueInDp = dpToPixels(context, 10f)
        marginYAxisAndValueInDp = dpToPixels(context, 20f)
        defaultBarWidth = dpToPixels(context, 15f).toInt()
        minBarHeight = dpToPixels(context, 35f).toInt()

        initPaint()
    }

    private fun initPaint() {
        paint.style = Paint.Style.FILL

        axisPaint.color = textColor
        axisPaint.strokeWidth = axisStrokeWidth

        textPaint.typeface = ResourcesCompat.getFont(context, textFontId)
        textPaint.textSize = dpToPixels(context, fontSize.toFloat())
        textPaint.color = textColor

        textLabelPaint.typeface = ResourcesCompat.getFont(context, textLabelFontId)
        textLabelPaint.textSize = dpToPixels(context, fontSize.toFloat())
        textLabelPaint.color = selectedBarTextLabelColor
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val upX = event.x
                val upY = event.y
                if (sqrt(((upX - tapDownX) * (upX - tapDownX) + (upY - tapDownY) * (upY - tapDownY)).toDouble()) < 10) {
                    onClick(event)
                }
            }
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return gestureDetector.onTouchEvent(event)
        }
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
        for (bar in barData) {
            val currentTextWidth = textPaint.measureText((bar.barValue).toString())
            if (maxWidthOfYAxisText < currentTextWidth) maxWidthOfYAxisText =
                currentTextWidth.toInt()

            textPaint.getTextBounds(bar.xAxisValue, 0, bar.xAxisValue.length, bounds)
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

        axisStartX = paddingLeft + maxWidthOfYAxisText + marginYAxisAndValueInDp.toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        val usableViewHeight = height - paddingBottom - paddingTop
        val usableViewWidth = width - paddingLeft - paddingRight
        val origin: Point = getOrigin()

        if (barData.isEmpty()) return

        barAndVacantSpaceCount = if (barData.size <= maxVisibleBarCount) {
            (barData.size * 2 shl 1)
        } else {
            maxVisibleBarCount * 2 shl 1
        }

        barWidth = if (barData.size < maxVisibleBarCount)
            defaultBarWidth else (usableViewWidth - maxWidthOfYAxisText) / barAndVacantSpaceCount
        barAxisWidth = origin.x + (barData.size * 4) * barWidth

        drawAxis(canvas, origin)

        barRects.clear()
        drawBarChart(canvas, usableViewHeight, origin)

        if (isFirstLaunch && barData.size > maxVisibleBarCount && currentMonthBarIndex >= maxVisibleBarCount) {
            scrollX = barRects[currentMonthBarIndex].left.toInt() - width / 2
            isFirstLaunch = false
            lastScrollPos = scrollX.toFloat()
        }
    }

    private fun drawAxis(canvas: Canvas, origin: Point) {
        canvas.drawLine(
            origin.x.toFloat(),
            origin.y.toFloat(),
            barAxisWidth.toFloat(),
            origin.y.toFloat(),
            axisPaint
        )
    }

    private fun drawBarChart(
        canvas: Canvas,
        usableViewHeight: Int,
        origin: Point
    ) {
        var x1: Int
        var x2: Int
        var y1: Int
        var y2: Int

        for (index in 0 until barData.size * 2 - 1) {
            val barRectIndex = index * 2
            val barDataIndex = index / 2

            if (index % 2 == 0) { // draw data bar
                x1 = origin.x + (barRectIndex + 1) * barWidth
                x2 = origin.x + (barRectIndex + 2) * barWidth
                var barHeight =
                    ((usableViewHeight - getXAxisLabelAndMargin()) * barData[barDataIndex].barValue / maxValueOfData).toInt()
                if (barHeight < minBarHeight) barHeight = minBarHeight
                y1 = origin.y - barHeight
                y2 = origin.y - marginXAxisAndValueInDp.toInt()

                val rect = RectF(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())
                barRects.add(rect)

                paint.color = barColor
                canvas.drawRoundRect(rect, barCornerRadius, barCornerRadius, paint)

                val barWidthInDp = pixelsToDp(context, barWidth.toFloat())
                paint.color = outerBarCircleColor
                canvas.drawCircle(
                    (x1 + x2) / 2f,
                    y1.toFloat() + barWidthInDp,
                    barWidthInDp,
                    paint
                )

                paint.color = innerBarCircleColor
                canvas.drawCircle(
                    (x1 + x2) / 2f,
                    y1.toFloat() + barWidthInDp,
                    barWidthInDp / 2f,
                    paint
                )

                showXAxisLabel(origin, barData[barDataIndex].xAxisValue, x1 + (x2 - x1) / 2, canvas)

                if (barData[barDataIndex].isCurrentMonth() && clickedBarRectIndex == -1) {
                    currentMonthBarIndex = barDataIndex
                    drawHighlightBar(canvas, barDataIndex)
                }

            } else { // draw empty bar
                x1 = origin.x + (barRectIndex + 1) * barWidth
                x2 = origin.x + (barRectIndex + 2) * barWidth
                var barHeight =
                    ((usableViewHeight - getXAxisLabelAndMargin()) * barData[barDataIndex].barValue / maxValueOfData)
                if (barHeight < minBarHeight) barHeight = minBarHeight
                y1 = origin.y - barHeight
                y2 = origin.y - marginXAxisAndValueInDp.toInt()

                val rect = RectF(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())
                paint.color = barColor
                canvas.drawRoundRect(rect, barCornerRadius, barCornerRadius, paint)
            }
        }

        if (isBarClicked && clickedBarRectIndex >= 0) drawHighlightBar(canvas, clickedBarRectIndex)

        drawYAxisLabels(origin, usableViewHeight - getXAxisLabelAndMargin(), canvas)
    }

    private fun drawHighlightBar(canvas: Canvas, barIndex: Int) {
        if (barIndex == -1) return

        val rect = barRects[barIndex]

        paint.color = selectedBarColor
        canvas.drawRoundRect(rect, barCornerRadius, barCornerRadius, paint)

        paint.color = innerBarCircleColor
        val barWidthInDp = pixelsToDp(context, barWidth.toFloat())
        canvas.drawCircle(
            rect.centerX(),
            rect.top + barWidthInDp,
            barWidthInDp / 2f,
            paint
        )

        drawHighlightBarLabel(canvas, rect, barData[barIndex].barValue.toString())
    }

    private fun drawHighlightBarLabel(canvas: Canvas, barRect: RectF, label: String) {
        val halfBar = barRect.width() / 2

        val labelTriangleTop = barRect.top - barRect.width()
        path.apply {
            reset()
            moveTo(barRect.centerX(), barRect.top - 10)
            lineTo(barRect.centerX() + halfBar, labelTriangleTop)
            lineTo(barRect.centerX() - halfBar, labelTriangleTop)
            close()
        }

        paint.color = selectedBarLabelColor
        canvas.drawPath(path, paint)

        val textBounds = Rect()
        textLabelPaint.getTextBounds(label, 0, label.length, textBounds)

        val margin = dpToPixels(context, 12f)
        val labelRect = RectF(
            barRect.centerX() - textBounds.width() / 2 - margin,
            labelTriangleTop - textBounds.height() - margin * 2,
            barRect.centerX() + textBounds.width() / 2 + margin,
            labelTriangleTop
        )
        canvas.drawRoundRect(labelRect, margin, margin, paint)

        textLabelPaint.color = selectedBarTextLabelColor
        canvas.drawText(
            label,
            labelRect.centerX() - textBounds.width() / 2,
            labelRect.centerY() + textBounds.height() / 2,
            textLabelPaint
        )
    }


    /**
     * Draws Y axis labels and marker points along Y axis.
     *
     * @param origin           coordinates of origin on canvas
     * @param usableViewHeight view height after removing the padding
     * @param canvas           canvas to draw the chart
     */
    private fun drawYAxisLabels(
        origin: Point,
        usableViewHeight: Int,
        canvas: Canvas
    ) {
        val yAxisValueInterval = usableViewHeight / maxValueCountOnYAxis
        val dataInterval = maxValueOfData / maxValueCountOnYAxis
        var valueToBeShown = maxValueOfData

        /*paint.color = Color.parseColor("#ffffff")
        canvas.drawRect(
            axisStartX - maxWidthOfYAxisText - marginYAxisAndValueInDp - 15,
            origin.y - height.toFloat(),
            axisStartX - marginYAxisAndValueInDp + 15,
            origin.y.toFloat() + marginXAxisAndValueInDp + maxHeightOfXAxisText,
            paint
        )*/


        //draw all texts from top to bottom
        for (index in 0 until maxValueCountOnYAxis) {
            val string = valueToBeShown.toString()

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
        val y = origin.y + marginXAxisAndValueInDp.toInt() + maxHeightOfXAxisText
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
        return if (barData.isNotEmpty()) {
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

    private fun onClick(event: MotionEvent) {
        val x = event.x + lastScrollPos
        val y = event.y

        clickedBarRectIndex = getRectIndexFor(x, y)
        isBarClicked = true
        invalidate()
    }

    private fun getRectIndexFor(x: Float, y: Float): Int {
        barRects.forEachIndexed { index, rectF ->
            if (rectF.contains(x, y)) return index
        }

        return -1 // x, y do not lie in our view
    }
}
