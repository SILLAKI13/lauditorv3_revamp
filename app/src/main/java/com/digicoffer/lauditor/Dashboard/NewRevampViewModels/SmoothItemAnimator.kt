package com.digicoffer.lauditor.Dashboard.NewRevampViewModels

import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class SmoothItemAnimator : DefaultItemAnimator() {

    init {
        addDuration = ADD_DURATION
        removeDuration = REMOVE_DURATION
        changeDuration = CHANGE_DURATION
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        val v = holder.itemView
        v.alpha = 0f
        v.translationY = TRANSLATE_Y_PX
        dispatchAddStarting(holder)

        v.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(ADD_DURATION)
            .setInterpolator(DecelerateInterpolator(1.6f))
            .withEndAction {
                v.alpha = 1f
                v.translationY = 0f
                dispatchAddFinished(holder)
            }
            .start()
        return true
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        val v = holder.itemView
        dispatchRemoveStarting(holder)

        v.animate()
            .alpha(0f)
            .translationY(-TRANSLATE_Y_PX * 0.5f)
            .setDuration(REMOVE_DURATION)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                v.alpha = 1f
                v.translationY = 0f
                dispatchRemoveFinished(holder)
            }
            .start()
        return true
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder?,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        dispatchChangeStarting(oldHolder, true)
        oldHolder.itemView.animate()
            .alpha(0f)
            .setDuration(CHANGE_DURATION / 2)
            .withEndAction {
                dispatchChangeFinished(oldHolder, true)
                if (newHolder != null && newHolder !== oldHolder) {
                    newHolder.itemView.alpha = 0f
                    newHolder.itemView.animate()
                        .alpha(1f)
                        .setDuration(CHANGE_DURATION / 2)
                        .withEndAction { dispatchChangeFinished(newHolder, false) }
                        .start()
                }
            }
            .start()
        return true
    }

    companion object {
        private const val ADD_DURATION = 300L
        private const val REMOVE_DURATION = 200L
        private const val CHANGE_DURATION = 180L
        private const val TRANSLATE_Y_PX = 30f
    }
}
