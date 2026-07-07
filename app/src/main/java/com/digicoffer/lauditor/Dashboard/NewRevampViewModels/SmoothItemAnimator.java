package com.digicoffer.lauditor.Dashboard.NewRevampViewModels;

import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

/**
 * SmoothItemAnimator
 *
 * Drop-in replacement for DefaultItemAnimator.
 * Cards slide up + fade in when added, fade out when removed.
 *
 * Usage:
 *   recyclerView.setItemAnimator(new SmoothItemAnimator());
 */
public class SmoothItemAnimator extends DefaultItemAnimator {

    private static final long  ADD_DURATION    = 300L;
    private static final long  REMOVE_DURATION = 200L;
    private static final long  CHANGE_DURATION = 180L;
    private static final float TRANSLATE_Y_PX  = 30f;

    public SmoothItemAnimator() {
        setAddDuration(ADD_DURATION);
        setRemoveDuration(REMOVE_DURATION);
        setChangeDuration(CHANGE_DURATION);
    }

    // ── Add ───────────────────────────────────────────────────────────────────

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        View v = holder.itemView;
        v.setAlpha(0f);
        v.setTranslationY(TRANSLATE_Y_PX);
        dispatchAddStarting(holder);

        v.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(ADD_DURATION)
                .setInterpolator(new DecelerateInterpolator(1.6f))
                .withEndAction(() -> {
                    v.setAlpha(1f);
                    v.setTranslationY(0f);
                    dispatchAddFinished(holder);
                })
                .start();
        return true;
    }

    // ── Remove ────────────────────────────────────────────────────────────────

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        View v = holder.itemView;
        dispatchRemoveStarting(holder);

        v.animate()
                .alpha(0f)
                .translationY(-TRANSLATE_Y_PX * 0.5f)
                .setDuration(REMOVE_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    v.setAlpha(1f);
                    v.setTranslationY(0f);
                    dispatchRemoveFinished(holder);
                })
                .start();
        return true;
    }

    // ── Change ────────────────────────────────────────────────────────────────

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder,
                                 RecyclerView.ViewHolder newHolder,
                                 int fromX, int fromY,
                                 int toX, int toY) {

        dispatchChangeStarting(oldHolder, true);
        oldHolder.itemView.animate()
                .alpha(0f)
                .setDuration(CHANGE_DURATION / 2)
                .withEndAction(() -> {
                    dispatchChangeFinished(oldHolder, true);
                    if (newHolder != null && newHolder != oldHolder) {
                        newHolder.itemView.setAlpha(0f);
                        newHolder.itemView.animate()
                                .alpha(1f)
                                .setDuration(CHANGE_DURATION / 2)
                                .withEndAction(() -> dispatchChangeFinished(newHolder, false))
                                .start();
                    }
                })
                .start();
        return true;
    }
}