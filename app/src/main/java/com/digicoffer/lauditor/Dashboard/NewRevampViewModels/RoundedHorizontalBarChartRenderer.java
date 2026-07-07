package com.digicoffer.lauditor.Dashboard.NewRevampViewModels;

import android.graphics.Canvas;
import android.graphics.RectF;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.HorizontalBarChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Draws horizontal bars as rounded capsules.
 * Only uses protected members — no private or non-existent method access.
 */
public class RoundedHorizontalBarChartRenderer extends HorizontalBarChartRenderer {

    private final float mRadius;
    private final RectF mRoundRect = new RectF();

    public RoundedHorizontalBarChartRenderer(
            BarDataProvider chart,
            ChartAnimator animator,
            ViewPortHandler viewPortHandler,
            float radiusPx) {
        super(chart, animator, viewPortHandler);
        mRadius = radiusPx;
    }

    @Override
    public void drawData(Canvas c) {
        BarData barData = mChart.getBarData();
        for (int i = 0; i < barData.getDataSetCount(); i++) {
            IBarDataSet set = barData.getDataSetByIndex(i);
            if (set.isVisible()) {
                drawDataSet(c, set, i);
            }
        }
    }

    @Override
    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {

        // mBarBuffers is protected — safe to access
        BarBuffer buffer = mBarBuffers[index];
        buffer.setPhases(mAnimator.getPhaseX(), mAnimator.getPhaseY());
        buffer.setDataSet(index);
        buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));
        buffer.setBarWidth(mChart.getBarData().getBarWidth());
        buffer.feed(dataSet);

        // Transform value coords → screen coords
        mChart.getTransformer(dataSet.getAxisDependency())
                .pointValuesToPixel(buffer.buffer);

        final boolean isSingleColor = dataSet.getColors().size() == 1;

        if (isSingleColor) {
            mRenderPaint.setColor(dataSet.getColor());
        }

        // Each bar = 4 floats: left, top, right, bottom
        for (int j = 0; j < buffer.size(); j += 4) {

            if (!mViewPortHandler.isInBoundsTop(buffer.buffer[j + 3])) break;
            if (!mViewPortHandler.isInBoundsBottom(buffer.buffer[j + 1])) continue;

            if (!isSingleColor) {
                mRenderPaint.setColor(dataSet.getColor(j / 4));
            }

            float left   = buffer.buffer[j];
            float top    = buffer.buffer[j + 1];
            float right  = buffer.buffer[j + 2];
            float bottom = buffer.buffer[j + 3];

            // Clamp radius to half bar height so it never over-rounds
            float radius = Math.min(mRadius, (bottom - top) / 2f);

            mRoundRect.set(left, top, right, bottom);
            c.drawRoundRect(mRoundRect, radius, radius, mRenderPaint);
        }
    }
}