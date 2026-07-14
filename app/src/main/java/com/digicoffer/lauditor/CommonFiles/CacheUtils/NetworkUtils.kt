package com.digicoffer.lauditor.CommonFiles.CacheUtils

import android.content.Context
import android.net.ConnectivityManager

class NetworkUtils {
    companion object {
        @JvmStatic
        fun isInternetAvailable(context: Context?): Boolean {
            if (context == null) return false
            return try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                val activeNetwork = cm?.activeNetworkInfo
                activeNetwork != null && activeNetwork.isConnected
            } catch (e: Exception) {
                false
            }
        }
    }
}
