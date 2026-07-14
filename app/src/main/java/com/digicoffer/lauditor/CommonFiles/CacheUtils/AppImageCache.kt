package com.digicoffer.lauditor.CommonFiles.CacheUtils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.LruCache
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.ByteArrayOutputStream

class AppImageCache {
    companion object {
        private val sMemoryCache: LruCache<String, Bitmap>
        private const val MAX_IMAGE_DIMENSION = 512
        private const val JPEG_QUALITY = 75

        init {
            val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
            val cacheSize = minOf(maxMemory / 8, 8 * 1024)

            sMemoryCache = object : LruCache<String, Bitmap>(cacheSize) {
                override fun sizeOf(key: String?, bitmap: Bitmap): Int {
                    return bitmap.byteCount / 1024
                }
            }
        }

        @JvmStatic
        fun preload(context: Context?, url: String?) {
            if (context == null || url.isNullOrEmpty()) return
            if (sMemoryCache.get(url) != null) return

            Glide.with(context.applicationContext)
                .asBitmap()
                .load(url)
                .apply(buildOptions())
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val compressed = compressAndCircleCrop(resource)
                        if (compressed != null) {
                            sMemoryCache.put(url, compressed)
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        @JvmStatic
        fun load(
            context: Context?,
            url: String?,
            imageView: ImageView?,
            name: String?,
            fallbackTextView: TextView?
        ) {
            if (context == null || imageView == null) return

            // No URL -> show only initial letter, hide image permanently
            if (url.isNullOrEmpty()) {
                imageView.visibility = View.GONE
                applyFallback(fallbackTextView, name, true)
                return
            }

            // Fast path: memory cache hit -> instant, no flicker
            val cached = sMemoryCache.get(url)
            if (cached != null) {
                imageView.setImageBitmap(cached)
                imageView.visibility = View.VISIBLE
                applyFallback(fallbackTextView, name, false)
                return
            }

            // Slow path: show initial letter FIRST while image loads
            imageView.visibility = View.GONE
            applyFallback(fallbackTextView, name, true)

            Glide.with(context.applicationContext)
                .asBitmap()
                .load(url)
                .apply(buildOptions())
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val compressed = compressAndCircleCrop(resource)
                        if (compressed == null) {
                            imageView.visibility = View.GONE
                            applyFallback(fallbackTextView, name, true)
                            return
                        }
                        sMemoryCache.put(url, compressed)
                        imageView.setImageBitmap(compressed)
                        imageView.visibility = View.VISIBLE
                        applyFallback(fallbackTextView, name, false)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        imageView.visibility = View.GONE
                        applyFallback(fallbackTextView, name, true)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        @JvmStatic
        fun invalidate(context: Context?, url: String?) {
            if (!url.isNullOrEmpty()) {
                sMemoryCache.remove(url)
            }
        }

        @JvmStatic
        fun clearAll() {
            sMemoryCache.evictAll()
        }

        private fun compressAndCircleCrop(source: Bitmap?): Bitmap? {
            if (source == null) return null
            val scaled = scaleBitmap(source, MAX_IMAGE_DIMENSION)
            val compressed = recompress(scaled)
            return circleCrop(compressed)
        }

        private fun scaleBitmap(source: Bitmap, maxDimension: Int): Bitmap {
            val width = source.width
            val height = source.height

            if (width <= maxDimension && height <= maxDimension) return source

            val scale = minOf(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
            val newWidth = Math.round(width * scale)
            val newHeight = Math.round(height * scale)

            return Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
        }

        private fun recompress(source: Bitmap?): Bitmap? {
            if (source == null) return null
            return try {
                val baos = ByteArrayOutputStream()
                source.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos)
                val bytes = baos.toByteArray()
                val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                decoded ?: source
            } catch (e: Exception) {
                source
            }
        }

        private fun circleCrop(source: Bitmap?): Bitmap? {
            if (source == null) return null
            val size = minOf(source.width, source.height)
            val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            paint.shader = shader
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
            return output
        }

        private fun buildOptions(): RequestOptions {
            return RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .skipMemoryCache(true)
                .override(MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION)
                .dontAnimate()
        }

        private fun applyFallback(fallbackTextView: TextView?, name: String?, show: Boolean) {
            if (fallbackTextView == null) return
            fallbackTextView.visibility = if (show) View.VISIBLE else View.GONE
            if (show) {
                fallbackTextView.text = if (!name.isNullOrEmpty()) name.substring(0, 1).uppercase() else "?"
            }
        }
    }
}
