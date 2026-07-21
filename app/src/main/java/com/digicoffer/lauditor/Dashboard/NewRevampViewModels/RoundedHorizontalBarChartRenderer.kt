package com.digicoffer.lauditor.Dashboard.NewRevampViewModels

import android.graphics.Canvas
import android.graphics.RectF
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.buffer.BarBuffer
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.HorizontalBarChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

class RoundedHorizontalBarChartRenderer(
    chart: BarDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler,
    private val mRadius: Float
) : HorizontalBarChartRenderer(chart, animator, viewPortHandler) {

    private val mRoundRect = RectF()

    override fun drawData(c: Canvas) {
        val barData = mChart.barData ?: return
        for (i in 0 until barData.dataSetCount) {
            val set = barData.getDataSetByIndex(i)
            if (set.isVisible) {
                drawDataSet(c, set, i)
            }
        }
    }

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        val buffer: BarBuffer = mBarBuffers[index]
        buffer.setPhases(mAnimator.phaseX, mAnimator.phaseY)
        buffer.setDataSet(index)
        buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
        buffer.setBarWidth(mChart.barData.barWidth)
        buffer.feed(dataSet)

        mChart.getTransformer(dataSet.axisDependency)
            .pointValuesToPixel(buffer.buffer)

        val isSingleColor = dataSet.colors.size == 1

        if (isSingleColor) {
            mRenderPaint.color = dataSet.color
        }

        var j = 0
        val size = buffer.size()
        while (j < size) {
            if (!mViewPortHandler.isInBoundsTop(buffer.buffer[j + 3])) break
            if (!mViewPortHandler.isInBoundsBottom(buffer.buffer[j + 1])) {
                j += 4
                continue
            }

            if (!isSingleColor) {
                mRenderPaint.color = dataSet.getColor(j / 4)
            }

            val left = buffer.buffer[j]
            val top = buffer.buffer[j + 1]
            val right = buffer.buffer[j + 2]
            val bottom = buffer.buffer[j + 3]

            val radius = Math.min(mRadius, (bottom - top) / 2f)

            mRoundRect.set(left, top, right, bottom)
            c.drawRoundRect(mRoundRect, radius, radius, mRenderPaint)
            j += 4
        }
    }
}
