package com.digicoffer.lauditor.FirmProfile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * CropImageView
 *
 * Custom View that displays a bitmap with:
 *  - Pinch-to-zoom (clamped to minScale–MAX_SCALE)
 *  - Drag to pan (clamped so image always covers the circle)
 *  - Rotation (via rotate() API)
 *  - Dark overlay with transparent circle cut-out
 *  - White border ring around the circle
 *  - Deferred bitmap loading (safe to call setImageBitmap() before layout)
 *  - OnZoomChangedListener so the host can sync a SeekBar
 *  - getCroppedBitmap(outputSize) → produces a circular-cropped square bitmap
 *
 * No changes from original — included in full so your project compiles cleanly.
 */
public class CropImageView extends View {

    public interface OnZoomChangedListener {
        void onZoomChanged(float zoom);
    }

    // ── State ──────────────────────────────────────────────────────────────
    private Bitmap sourceBitmap;
    private float  scale    = 1f;
    private float  rotation = 0f;
    private float  transX   = 0f, transY = 0f;
    private float  minScale = 1f;
    private static final float MAX_SCALE = 5f;

    // Deferred bitmap (set before view is laid out)
    private Bitmap pendingBitmap = null;

    // ── Touch ──────────────────────────────────────────────────────────────
    private ScaleGestureDetector scaleDetector;
    private float lastTouchX, lastTouchY;
    private int   activePointerId = MotionEvent.INVALID_POINTER_ID;

    // ── Paint ──────────────────────────────────────────────────────────────
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint overlayPaint;
    private final Paint circlePaint;
    private final Paint borderPaint;

    // ── Listener ───────────────────────────────────────────────────────────
    private OnZoomChangedListener zoomListener;

    // ── Constructors ───────────────────────────────────────────────────────
    public CropImageView(Context ctx) { this(ctx, null); }
    public CropImageView(Context ctx, AttributeSet attrs) { this(ctx, attrs, 0); }
    public CropImageView(Context ctx, AttributeSet attrs, int def) {
        super(ctx, attrs, def);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        overlayPaint = new Paint();
        overlayPaint.setColor(Color.parseColor("#99000000"));

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.TRANSPARENT);
        circlePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        borderPaint.setAlpha(200);

        scaleDetector = new ScaleGestureDetector(ctx,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector d) {
                        float newScale = Math.max(minScale,
                                Math.min(MAX_SCALE, scale * d.getScaleFactor()));
                        float focusX  = d.getFocusX() - getWidth()  / 2f;
                        float focusY  = d.getFocusY() - getHeight() / 2f;
                        float delta   = newScale / scale;
                        transX = focusX + (transX - focusX) * delta;
                        transY = focusY + (transY - focusY) * delta;
                        scale  = newScale;
                        clampTranslation();
                        notifyZoom();
                        invalidate();
                        return true;
                    }
                });
    }

    // ── Public API ─────────────────────────────────────────────────────────

    public void setImageBitmap(Bitmap bm) {
        if (bm == null) return;
        if (getWidth() == 0 || getHeight() == 0) {
            pendingBitmap = bm;
            return;
        }
        applyBitmap(bm);
    }

    private void applyBitmap(Bitmap bm) {
        sourceBitmap = bm;
        rotation = 0f;
        transX   = 0f;
        transY   = 0f;
        recalcMinScale();
        scale = minScale;   // start exactly filling the circle
        notifyZoom();
        invalidate();
    }

    public void setOnZoomChangedListener(OnZoomChangedListener l) {
        zoomListener = l;
    }

    /**
     * Called from SeekBar. progress 0–max → scale minScale–MAX_SCALE.
     */
    public void setZoomFromSeek(int progress, int max) {
        if (sourceBitmap == null) return;
        scale = minScale + (MAX_SCALE - minScale) * ((float) progress / max);
        clampTranslation();
        invalidate();
    }

    public float getMinScale()     { return minScale; }
    public float getMaxScale()     { return MAX_SCALE; }
    public float getCurrentScale() { return scale; }

    public void rotate(float degrees) {
        rotation = (rotation + degrees + 360f) % 360f;
        transX   = 0f;
        transY   = 0f;
        recalcMinScale();
        scale = Math.max(scale, minScale);
        clampTranslation();
        notifyZoom();
        invalidate();
    }

    /**
     * Returns a circular-cropped square bitmap at the given pixel size.
     */
    public Bitmap getCroppedBitmap(int outputSize) {
        if (sourceBitmap == null) return null;
        float r      = Math.min(getWidth(), getHeight()) / 2f;
        float factor = outputSize / (2f * r);

        Bitmap out = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888);
        Canvas c   = new Canvas(out);
        c.drawColor(Color.WHITE);
        android.graphics.Path path = new android.graphics.Path();
        path.addCircle(outputSize / 2f, outputSize / 2f, outputSize / 2f,
                android.graphics.Path.Direction.CW);
        c.clipPath(path);

        Matrix m = new Matrix();
        m.postTranslate(-sourceBitmap.getWidth() / 2f, -sourceBitmap.getHeight() / 2f);
        m.postScale(scale * factor, scale * factor);
        m.postRotate(rotation);
        m.postTranslate(outputSize / 2f + transX * factor,
                outputSize / 2f + transY * factor);
        c.drawBitmap(sourceBitmap, m, bitmapPaint);
        return out;
    }

    // ── Layout ─────────────────────────────────────────────────────────────

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (pendingBitmap != null) {
            applyBitmap(pendingBitmap);
            pendingBitmap = null;
        } else if (sourceBitmap != null) {
            recalcMinScale();
            scale = Math.max(scale, minScale);
            clampTranslation();
        }
    }

    private void recalcMinScale() {
        if (sourceBitmap == null || getWidth() == 0 || getHeight() == 0) return;
        float r   = Math.min(getWidth(), getHeight()) / 2f;
        float rad = (float)(rotation * Math.PI / 180.0);
        float cos = (float) Math.abs(Math.cos(rad));
        float sin = (float) Math.abs(Math.sin(rad));
        float rw  = sourceBitmap.getWidth()  * cos + sourceBitmap.getHeight() * sin;
        float rh  = sourceBitmap.getWidth()  * sin + sourceBitmap.getHeight() * cos;
        minScale  = Math.max(2f * r / rw, 2f * r / rh) * 1.005f;
    }

    // ── Draw ───────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int   w  = getWidth();
        int   h  = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;
        float r  = Math.min(cx, cy) - 1f;

        // 1. Bitmap
        if (sourceBitmap != null) {
            canvas.save();
            canvas.drawBitmap(sourceBitmap, buildMatrix(cx, cy), bitmapPaint);
            canvas.restore();
        }

        // 2. Dark overlay with transparent circle cut-out
        Bitmap overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas oc      = new Canvas(overlay);
        oc.drawRect(0, 0, w, h, overlayPaint);
        oc.drawCircle(cx, cy, r, circlePaint);
        canvas.drawBitmap(overlay, 0, 0, null);
        overlay.recycle();

        // 3. White border ring
        canvas.drawCircle(cx, cy, r, borderPaint);
    }

    private Matrix buildMatrix(float cx, float cy) {
        Matrix m = new Matrix();
        if (sourceBitmap == null) return m;
        m.postTranslate(-sourceBitmap.getWidth() / 2f, -sourceBitmap.getHeight() / 2f);
        m.postScale(scale, scale);
        m.postRotate(rotation);
        m.postTranslate(cx + transX, cy + transY);
        return m;
    }

    // ── Touch ──────────────────────────────────────────────────────────────

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        scaleDetector.onTouchEvent(e);
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = e.getPointerId(0);
                lastTouchX = e.getX(0);
                lastTouchY = e.getY(0);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!scaleDetector.isInProgress()
                        && activePointerId != MotionEvent.INVALID_POINTER_ID) {
                    int idx = e.findPointerIndex(activePointerId);
                    if (idx >= 0) {
                        transX += e.getX(idx) - lastTouchX;
                        transY += e.getY(idx) - lastTouchY;
                        lastTouchX = e.getX(idx);
                        lastTouchY = e.getY(idx);
                        clampTranslation();
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP: {
                int upId = e.getPointerId(e.getActionIndex());
                if (upId == activePointerId) {
                    int newIdx = e.getActionIndex() == 0 ? 1 : 0;
                    if (newIdx < e.getPointerCount()) {
                        activePointerId = e.getPointerId(newIdx);
                        lastTouchX = e.getX(newIdx);
                        lastTouchY = e.getY(newIdx);
                    } else {
                        activePointerId = MotionEvent.INVALID_POINTER_ID;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activePointerId = MotionEvent.INVALID_POINTER_ID;
                break;
        }
        return true;
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void clampTranslation() {
        if (sourceBitmap == null || getWidth() == 0) return;
        float r   = Math.min(getWidth(), getHeight()) / 2f;
        float rad = (float)(rotation * Math.PI / 180.0);
        float cos = (float) Math.abs(Math.cos(rad));
        float sin = (float) Math.abs(Math.sin(rad));
        float hw  = (sourceBitmap.getWidth()  * cos + sourceBitmap.getHeight() * sin) * scale / 2f;
        float hh  = (sourceBitmap.getWidth()  * sin + sourceBitmap.getHeight() * cos) * scale / 2f;
        transX = Math.max(-(hw - r), Math.min(hw - r, transX));
        transY = Math.max(-(hh - r), Math.min(hh - r, transY));
    }

    private void notifyZoom() {
        if (zoomListener != null) zoomListener.onZoomChanged(scale);
    }
}