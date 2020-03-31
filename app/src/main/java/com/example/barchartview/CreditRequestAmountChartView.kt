package com.example.barchartview

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat

class CreditRequestAmountChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {

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
            findMaxHeightOfBarText()
            barAndVacantSpaceCount = (barData.size * 8f)
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

    var textFontId: Int = R.font.roboto_medium
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val textValuePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var marginXAxisAndValueInDp = 0f
    private var maxHeightOBarValueText = 0

    private var maxValueOfData: Int = 0

    private var barAndVacantSpaceCount = 0f
    private var barWidth = 0f
    private var minBarHeight = 0f
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
        minBarHeight = dpToPixels(context, 30f)

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


    private fun findMaxHeightOfBarText() {
        maxHeightOBarValueText = Int.MIN_VALUE

        val bounds = Rect()
        for (bar in barData) {
            val text = bar.barValue.toString()
            textValuePaint.getTextBounds(text, 0, text.length, bounds)
            if (maxHeightOBarValueText < bounds.height()) maxHeightOBarValueText = bounds.height()
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

        barWidth = usableViewWidth / barAndVacantSpaceCount
        barAxisWidth = origin.x + usableViewWidth

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
        var x1: Float
        var x2: Float
        var y1: Float
        var y2: Float

        for (index in 0 until barData.size * 3 - 2) {
            val barRectIndex = index * 3
            val barDataIndex = index / 3

            if (index % 3 == 0) { // draw data bar
                x1 = origin.x + (barRectIndex) * barWidth
                x2 = origin.x + (barRectIndex + 1) * barWidth
                var barHeight: Float =
                    ((usableViewHeight - marginXAxisAndValueInDp - maxHeightOBarValueText * 2) * barData[barDataIndex].barValue / maxValueOfData)

                val barWidthInDp = pixelsToDp(context, barWidth)
                if (barHeight == 0f) {
                    val circleY = origin.y.toFloat() - marginXAxisAndValueInDp - barWidthInDp
                    barHeight = (marginXAxisAndValueInDp + barWidthInDp)

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
                    y2 = origin.y - marginXAxisAndValueInDp

                    val rect = RectF(x1, y1, x2, y2)

                    paint.color = Color.parseColor(barData[barDataIndex].getBarColor())
                    canvas.drawRoundRect(rect, barCornerRadius, barCornerRadius, paint)

                    paint.color = circleBarColor
                    canvas.drawCircle(
                        (x1 + x2) / 2f,
                        y1 + barWidthInDp,
                        barWidthInDp / 2f,
                        paint
                    )
                }

                drawXAxisLabel(
                    origin,
                    barData[barDataIndex].getDatePair(),
                    x1 + (x2 - x1) / 2,
                    canvas
                )
                drawBarValue(
                    origin,
                    barData[barDataIndex].barValue,
                    x1 + (x2 - x1) / 2,
                    barHeight,
                    canvas
                )
            } else { // draw empty bar
                x1 = origin.x + (barRectIndex) * barWidth
                x2 = origin.x + (barRectIndex + 1) * barWidth
                var barHeight =
                    ((usableViewHeight - marginXAxisAndValueInDp - maxHeightOBarValueText * 2) * barData[barDataIndex + 1].barValue / maxValueOfData)
                if (barHeight < minBarHeight) barHeight = minBarHeight
                y1 = origin.y - barHeight
                y2 = origin.y - marginXAxisAndValueInDp

                val rect = RectF(x1, y1, x2, y2)
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
        label: Pair<String, String>,
        centerX: Float,
        canvas: Canvas
    ) {
        val digit = label.first
        val day = label.second
        val text = "$digit $day"
        textPaint.textAlign = Paint.Align.CENTER
        val textWidth = textPaint.measureText(day).toInt()

        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder
                .obtain(text, 0, text.length, textPaint, textWidth)
                .build()
        } else {
            StaticLayout(
                text,
                textPaint,
                textWidth,
                Layout.Alignment.ALIGN_NORMAL,
                1F,
                0F,
                false
            )
        }

        val y = origin.y + marginXAxisAndValueInDp
        staticLayout.draw(canvas, centerX, y)
    }

    private fun drawBarValue(
        origin: Point,
        value: Int,
        centerX: Float,
        barHeight: Float,
        canvas: Canvas
    ) {
        val bounds = Rect()
        val text = value.toString()
        textValuePaint.getTextBounds(text, 0, text.length, bounds)
        val y = origin.y - barHeight - bounds.height()
        val x = centerX - bounds.width() / 2

        canvas.drawText(text, x, y, textValuePaint)
    }

    /**
     * Returns the origin coordinates in canvas' coordinates.
     *
     * @return origin's coordinates
     */
    private fun getOrigin(): Point {
        val startPadding = dpToPixels(context, 10f)
        return if (barData.isNotEmpty()) {
            Point(
                paddingLeft + startPadding.toInt(),
                height - paddingBottom - marginXAxisAndValueInDp.toInt()
            )
        } else {
            Point(
                paddingLeft + startPadding.toInt(),
                height - paddingBottom
            )
        }
    }
}
