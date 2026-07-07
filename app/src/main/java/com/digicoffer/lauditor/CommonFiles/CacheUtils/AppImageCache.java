package com.digicoffer.lauditor.CommonFiles.CacheUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class AppImageCache {

    private static final LruCache<String, Bitmap> sMemoryCache;

    // ── Compression settings — aggressive for fast load ───────────────────
    private static final int MAX_IMAGE_DIMENSION = 512; // smaller = faster
    private static final int JPEG_QUALITY = 75;  // lower = smaller file

    static {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = Math.min(maxMemory / 8, 8 * 1024);

        sMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public static void preload(Context context, String url) {
        if (context == null || TextUtils.isEmpty(url)) return;
        if (sMemoryCache.get(url) != null) return;

        Glide.with(context.getApplicationContext())
                .asBitmap()
                .load(url)
                .apply(buildOptions())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource,
                                                @Nullable Transition<? super Bitmap> transition) {
                        Bitmap compressed = compressAndCircleCrop(resource);
                        if (compressed != null) sMemoryCache.put(url, compressed);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    public static void load(Context context, String url,
                            ImageView imageView, String name,
                            @Nullable TextView fallbackTextView) {

        if (context == null || imageView == null) return;

        // No URL → show only initial letter, hide image permanently
        if (TextUtils.isEmpty(url)) {
            imageView.setVisibility(View.GONE);
            applyFallback(fallbackTextView, name, true);
            return;
        }

        // ── Fast path: memory cache hit → instant, no flicker ─────────────
        Bitmap cached = sMemoryCache.get(url);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            imageView.setVisibility(View.VISIBLE);
            applyFallback(fallbackTextView, name, false);
            return;
        }

        // ── Slow path: show initial letter FIRST while image loads ─────────
        // ✅ CORRECT UX: user sees their initial immediately,
        //    image fades in on top when ready — never blank, never double-view
        imageView.setVisibility(View.GONE);        // hide image slot
        applyFallback(fallbackTextView, name, true);     // show initial letter now

        Glide.with(context.getApplicationContext())
                .asBitmap()
                .load(url)
                .apply(buildOptions())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource,
                                                @Nullable Transition<? super Bitmap> transition) {
                        Bitmap compressed = compressAndCircleCrop(resource);
                        if (compressed == null) {
                            // Compression failed — keep showing initial
                            imageView.setVisibility(View.GONE);
                            applyFallback(fallbackTextView, name, true);
                            return;
                        }
                        sMemoryCache.put(url, compressed);
                        // ✅ Image ready — swap: hide initial, show image
                        imageView.setImageBitmap(compressed);
                        imageView.setVisibility(View.VISIBLE);
                        applyFallback(fallbackTextView, name, false);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        // Keep showing initial letter on failure
                        imageView.setVisibility(View.GONE);
                        applyFallback(fallbackTextView, name, true);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    public static void invalidate(Context context, String url) {
        if (!TextUtils.isEmpty(url)) {
            sMemoryCache.remove(url);
        }
    }

    public static void clearAll() {
        sMemoryCache.evictAll();
    }

    // ── Compression pipeline ──────────────────────────────────────────────

    private static Bitmap compressAndCircleCrop(Bitmap source) {
        if (source == null) return null;
        Bitmap scaled = scaleBitmap(source, MAX_IMAGE_DIMENSION);
        Bitmap compressed = recompress(scaled);
        return circleCrop(compressed);
    }

    private static Bitmap scaleBitmap(Bitmap source, int maxDimension) {
        int width = source.getWidth();
        int height = source.getHeight();

        if (width <= maxDimension && height <= maxDimension) return source;

        float scale = Math.min((float) maxDimension / width,
                (float) maxDimension / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }

    private static Bitmap recompress(Bitmap source) {
        if (source == null) return null;
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            source.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos);
            byte[] bytes = baos.toByteArray();
            Bitmap decoded = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            return decoded != null ? decoded : source;
        } catch (Exception e) {
            return source;
        }
    }

    private static Bitmap circleCrop(Bitmap source) {
        if (source == null) return null;
        int size = Math.min(source.getWidth(), source.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        BitmapShader shader = new BitmapShader(
                source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        return output;
    }

    private static RequestOptions buildOptions() {
        return new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .skipMemoryCache(true)
                .override(MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION) // ✅ tell Glide to downscale before delivery
                .dontAnimate();
    }

    private static void applyFallback(@Nullable TextView fallbackTextView, String name, boolean show) {
        if (fallbackTextView == null) return;
        fallbackTextView.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            fallbackTextView.setText(
                    !TextUtils.isEmpty(name) ? name.substring(0, 1).toUpperCase() : "?");
        }
    }
}
