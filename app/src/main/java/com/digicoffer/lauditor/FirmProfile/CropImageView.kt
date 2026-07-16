package com.digicoffer.lauditor.FirmProfile

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CropImageView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(ctx, attrs, defStyleAttr) {

    interface OnZoomChangedListener {
        fun onZoomChanged(zoom: Float)
    }

    private var sourceBitmap: Bitmap? = null
    private var scale = 1f
    private var rotation = 0f
    private var transX = 0f
    private var transY = 0f
    private var minScale = 1f

    private var pendingBitmap: Bitmap? = null

    private val scaleDetector: ScaleGestureDetector
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = MotionEvent.INVALID_POINTER_ID

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val overlayPaint = Paint()
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var zoomListener: OnZoomChangedListener? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        overlayPaint.color = Color.parseColor("#99000000")

        circlePaint.color = Color.TRANSPARENT
        circlePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        borderPaint.color = Color.WHITE
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 2f
        borderPaint.alpha = 200

        scaleDetector = ScaleGestureDetector(ctx, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val newScale = max(minScale, min(MAX_SCALE, scale * detector.scaleFactor))
                val focusX = detector.focusX - width / 2f
                val focusY = detector.focusY - height / 2f
                val delta = newScale / scale
                transX = focusX + (transX - focusX) * delta
                transY = focusY + (transY - focusY) * delta
                scale = newScale
                clampTranslation()
                notifyZoom()
                invalidate()
                return true
            }
        })
    }

    fun setImageBitmap(bm: Bitmap?) {
        if (bm == null) return
        if (width == 0 || height == 0) {
            pendingBitmap = bm
            return
        }
        applyBitmap(bm)
    }

    private fun applyBitmap(bm: Bitmap) {
        sourceBitmap = bm
        rotation = 0f
        transX = 0f
        transY = 0f
        recalcMinScale()
        scale = minScale
        notifyZoom()
        invalidate()
    }

    fun setOnZoomChangedListener(l: OnZoomChangedListener?) {
        zoomListener = l
    }

    fun setZoomFromSeek(progress: Int, max: Int) {
        if (sourceBitmap == null) return
        scale = minScale + (MAX_SCALE - minScale) * (progress.toFloat() / max)
        clampTranslation()
        invalidate()
    }

    fun getMinScale(): Float = minScale
    fun getMaxScale(): Float = MAX_SCALE
    fun getCurrentScale(): Float = scale

    fun rotate(degrees: Float) {
        rotation = (rotation + degrees + 360f) % 360f
        transX = 0f
        transY = 0f
        recalcMinScale()
        scale = max(scale, minScale)
        clampTranslation()
        notifyZoom()
        invalidate()
    }

    fun getCroppedBitmap(outputSize: Int): Bitmap? {
        val src = sourceBitmap ?: return null
        val r = min(width, height) / 2f
        val factor = outputSize / (2f * r)

        val out = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.WHITE)
        val path = Path()
        path.addCircle(
            outputSize / 2f,
            outputSize / 2f,
            outputSize / 2f,
            Path.Direction.CW
        )
        canvas.clipPath(path)

        val m = Matrix()
        m.postTranslate(-src.width / 2f, -src.height / 2f)
        m.postScale(scale * factor, scale * factor)
        m.postRotate(rotation)
        m.postTranslate(
            outputSize / 2f + transX * factor,
            outputSize / 2f + transY * factor
        )
        canvas.drawBitmap(src, m, bitmapPaint)
        return out
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val pending = pendingBitmap
        if (pending != null) {
            applyBitmap(pending)
            pendingBitmap = null
        } else if (sourceBitmap != null) {
            recalcMinScale()
            scale = max(scale, minScale)
            clampTranslation()
        }
    }

    private fun recalcMinScale() {
        val src = sourceBitmap ?: return
        if (width == 0 || height == 0) return
        val r = min(width, height) / 2f
        val rad = (rotation * Math.PI / 180.0).toFloat()
        val cosVal = abs(Math.cos(rad.toDouble())).toFloat()
        val sinVal = abs(Math.sin(rad.toDouble())).toFloat()
        val rw = src.width * cosVal + src.height * sinVal
        val rh = src.width * sinVal + src.height * cosVal
        minScale = max(2f * r / rw, 2f * r / rh) * 1.005f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width
        val h = height
        val cx = w / 2f
        val cy = h / 2f
        val r = min(cx, cy) - 1f

        val src = sourceBitmap
        if (src != null) {
            canvas.save()
            canvas.drawBitmap(src, buildMatrix(cx, cy), bitmapPaint)
            canvas.restore()
        }

        val overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val oc = Canvas(overlay)
        oc.drawRect(0f, 0f, w.toFloat(), h.toFloat(), overlayPaint)
        oc.drawCircle(cx, cy, r, circlePaint)
        canvas.drawBitmap(overlay, 0f, 0f, null)
        overlay.recycle()

        canvas.drawCircle(cx, cy, r, borderPaint)
    }

    private fun buildMatrix(cx: Float, cy: Float): Matrix {
        val m = Matrix()
        val src = sourceBitmap ?: return m
        m.postTranslate(-src.width / 2f, -src.height / 2f)
        m.postScale(scale, scale)
        m.postRotate(rotation)
        m.postTranslate(cx + transX, cy + transY)
        return m
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(e)
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = e.getPointerId(0)
                lastTouchX = e.getX(0)
                lastTouchY = e.getY(0)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress && activePointerId != MotionEvent.INVALID_POINTER_ID) {
                    val idx = e.findPointerIndex(activePointerId)
                    if (idx >= 0) {
                        transX += e.getX(idx) - lastTouchX
                        transY += e.getY(idx) - lastTouchY
                        lastTouchX = e.getX(idx)
                        lastTouchY = e.getY(idx)
                        clampTranslation()
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val upId = e.getPointerId(e.actionIndex)
                if (upId == activePointerId) {
                    val newIdx = if (e.actionIndex == 0) 1 else 0
                    if (newIdx < e.pointerCount) {
                        activePointerId = e.getPointerId(newIdx)
                        lastTouchX = e.getX(newIdx)
                        lastTouchY = e.getY(newIdx)
                    } else {
                        activePointerId = MotionEvent.INVALID_POINTER_ID
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = MotionEvent.INVALID_POINTER_ID
            }
        }
        return true
    }

    private fun clampTranslation() {
        val src = sourceBitmap ?: return
        if (width == 0) return
        val r = min(width, height) / 2f
        val rad = (rotation * Math.PI / 180.0).toFloat()
        val cosVal = abs(Math.cos(rad.toDouble())).toFloat()
        val sinVal = abs(Math.sin(rad.toDouble())).toFloat()
        val hw = (src.width * cosVal + src.height * sinVal) * scale / 2f
        val hh = (src.width * sinVal + src.height * cosVal) * scale / 2f
        transX = max(-(hw - r), min(hw - r, transX))
        transY = max(-(hh - r), min(hh - r, transY))
    }

    private fun notifyZoom() {
        zoomListener?.onZoomChanged(scale)
    }

    companion object {
        private const val MAX_SCALE = 5f
    }
}
