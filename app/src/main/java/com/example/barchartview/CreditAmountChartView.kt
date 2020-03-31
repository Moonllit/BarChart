package com.example.barchartview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat

class CreditAmountChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {

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
            findMaxHeightOfXAxisText()
            invalidate()
        }

    var emptyBarColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var circleBarColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var textColor: Int = 0
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

    var maxVisibleBarCount: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textValuePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var marginXAxisAndValueInDp = 0f

    private var maxValueOfData: Int = 0

    private var maxHeightOfXAxisText = 0

    private var barAndVacantSpaceCount = 0
    private var barWidth = 0
    private var defaultBarWidth = 0
    private var minBarHeight = 0
    private var barAxisWidth = 0

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CreditAmountChartView,
            0, 0
        ).apply {
            try {
                circleBarColor =
                    getColor(
                        R.styleable.CreditAmountChartView_circleBarColor,
                        Color.parseColor("#ffffff")
                    )
                emptyBarColor =
                    getColor(
                        R.styleable.CreditAmountChartView_emptyBarColor,
                        Color.parseColor("#e1e5e7")
                    )
                textColor =
                    getColor(
                        R.styleable.CreditAmountChartView_textColor,
                        Color.parseColor("#96a6a7")
                    )
                maxVisibleBarCount = getInteger(
                    R.styleable.CreditAmountChartView_maxVisibleBarCount,
                    6
                )
                barCornerRadius = getDimension(
                    R.styleable.CreditAmountChartView_barCornerRadius,
                    20F
                )
                axisStrokeWidth = getDimension(
                    R.styleable.CreditAmountChartView_axisStrokeWidth,
                    3F
                )
            } finally {
                recycle()
            }
        }

        marginXAxisAndValueInDp = dpToPixels(context, 10f)
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

        textValuePaint.typeface = Typeface.DEFAULT_BOLD
        textValuePaint.textSize = dpToPixels(context, fontSize.toFloat())
        textValuePaint.color = Color.parseColor("#2c3e50")
    }

    /**
     * Calculate the maximum width occupied by any of given bar chart data. Width is calculated
     * based on font used and its size.
     */
    private fun findMaxHeightOfXAxisText() {
        maxHeightOfXAxisText = Int.MIN_VALUE

        val bounds = Rect()
        for (bar in barData) {
            textPaint.getTextBounds(bar.xAxisValue, 0, bar.xAxisValue.length, bounds)
            if (maxHeightOfXAxisText < bounds.height()) maxHeightOfXAxisText = bounds.height()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defaultSize = 200

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

        if (barData.isEmpty()) return

        barAndVacantSpaceCount = if (barData.size <= maxVisibleBarCount) {
            (barData.size * 2 shl 1)
        } else {
            maxVisibleBarCount * 2 shl 1
        }

        barWidth = if (barData.size < maxVisibleBarCount)
            defaultBarWidth else usableViewWidth / barAndVacantSpaceCount
        barAxisWidth = origin.x + (barData.size * 4) * barWidth

        drawAxis(canvas, origin)

        drawBarChart(canvas, usableViewHeight, origin)
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
                    ((usableViewHeight - getXAxisLabelAndMargin()) * barData[barDataIndex].barValue / maxValueOfData)

                val barWidthInDp = pixelsToDp(context, barWidth.toFloat())
                if (barHeight == 0) {
                    val circleY = origin.y.toFloat() - marginXAxisAndValueInDp - barWidthInDp
                    barHeight = (marginXAxisAndValueInDp + barWidthInDp).toInt()

                    paint.color = emptyBarColor
                    canvas.drawCircle(
                        (x1 + x2) / 2f,
                        circleY,
                        barWidthInDp,
                        paint
                    )

                    paint.color = circleBarColor
                    canvas.drawCircle(
                        (x1 + x2) / 2f,
                        circleY,
                        barWidthInDp / 2f,
                        paint
                    )
                } else {
                    if (barHeight < minBarHeight) barHeight = minBarHeight

                    y1 = origin.y - barHeight
                    y2 = origin.y - marginXAxisAndValueInDp.toInt()

                    val rect = RectF(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())

                    paint.color = Color.parseColor(barData[barDataIndex].getBarColor())
                    canvas.drawRoundRect(rect, barCornerRadius, barCornerRadius, paint)

                    paint.color = circleBarColor
                    canvas.drawCircle(
                        (x1 + x2) / 2f,
                        y1.toFloat() + barWidthInDp,
                        barWidthInDp / 2f,
                        paint
                    )
                }

                drawXAxisLabel(origin, barData[barDataIndex].xAxisValue, x1 + (x2 - x1) / 2, canvas)
                drawBarValue(
                    origin,
                    barData[barDataIndex].barValue,
                    x1 + (x2 - x1) / 2,
                    barHeight,
                    canvas
                )
            } else { // draw empty bar
                x1 = origin.x + (barRectIndex + 1) * barWidth
                x2 = origin.x + (barRectIndex + 2) * barWidth
                var barHeight =
                    ((usableViewHeight - getXAxisLabelAndMargin()) * barData[barDataIndex].barValue / maxValueOfData)
                if (barHeight < minBarHeight) barHeight = minBarHeight
                y1 = origin.y - barHeight
                y2 = origin.y - marginXAxisAndValueInDp.toInt()

                val rect = RectF(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())
                paint.color = emptyBarColor
                canvas.drawRoundRect(rect, barCornerRadius, barCornerRadius, paint)
            }
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
    private fun drawXAxisLabel(
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

    private fun drawBarValue(
        origin: Point,
        value: Int,
        centerX: Int,
        barHeight: Int,
        canvas: Canvas
    ) {
        val margin = dpToPixels(context, 10f)
        val bounds = Rect()
        val text = value.toString()
        textValuePaint.getTextBounds(text, 0, text.length, bounds)
        val y = origin.y - barHeight - margin
        val x = centerX - bounds.width() / 2

        canvas.drawText(text, x.toFloat(), y, textValuePaint)
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
                paddingLeft,
                height - paddingBottom - getXAxisLabelAndMargin()
            )
        } else {
            Point(
                paddingLeft,
                height - paddingBottom
            )
        }
    }
}
