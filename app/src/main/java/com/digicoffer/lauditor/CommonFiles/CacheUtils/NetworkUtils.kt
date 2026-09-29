package com.digicoffer.lauditor.CommonFiles.CacheUtils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class NetworkUtils {
    companion object {
        @JvmStatic
        fun isInternetAvailable(context: Context?): Boolean {
            if (context == null) return false
            return try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val activeNetwork = cm.activeNetwork ?: return false
                    val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                } else {
                    @Suppress("DEPRECATION")
                    val activeNetworkInfo = cm.activeNetworkInfo
                    @Suppress("DEPRECATION")
                    activeNetworkInfo != null && activeNetworkInfo.isConnected
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}
