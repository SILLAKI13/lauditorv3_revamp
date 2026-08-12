package com.digicoffer.lauditor.CommonFiles.GlobalFiles

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging

object FirebaseInitHelper {

    private const val TAG = "FirebaseInitHelper"
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    /**
     * Initialize Firebase dynamically based on build flavor/environment
     * Call this in LoginActivity.onCreate() BEFORE any Firebase operations
     */
    @JvmStatic
    fun initializeFirebase(context: Context) {
        try {
            // Check if Firebase is already initialized
            if (FirebaseApp.getApps(context).isEmpty()) {

                // Get the appropriate Firebase config based on environment
                val apiKey = getFirebaseApiKey()
                val projectId = getFirebaseProjectId()
                val appId = getFirebaseAppId()
                val databaseUrl = getFirebaseDatabaseUrl()
                val storageBucket = getFirebaseStorageBucket()

                Log.d(TAG, "Initializing Firebase with config:")
                Log.d(TAG, "  projectId: $projectId")
                Log.d(TAG, "  appId: $appId")
                Log.d(TAG, "  Environment: ${getCurrentEnvironment()}")

                // Build FirebaseOptions with appropriate credentials
                val options = FirebaseOptions.Builder()
                    .setApiKey(apiKey)
                    .setProjectId(projectId)
                    .setApplicationId(appId)
                    .setDatabaseUrl(databaseUrl)
                    .setStorageBucket(storageBucket)
                    .build()

                // Initialize Firebase with these options
                FirebaseApp.initializeApp(context, options)

                Log.d(TAG, "Firebase initialized successfully ✅")

                // Initialize Firebase Analytics
                initializeAnalytics(context)

                // Initialize FCM Token
                initializeFCM()

            } else {
                Log.d(TAG, "Firebase already initialized")
                initializeAnalytics(context)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase: " + e.message)
            e.printStackTrace()
        }
    }

    /**
     * Initialize Firebase Analytics with dynamic settings
     */
    @JvmStatic
    private fun initializeAnalytics(context: Context) {
        try {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)

            // Set user properties
            mFirebaseAnalytics?.setUserId(Constants.USER_ID)
            mFirebaseAnalytics?.setUserProperty("environment", getCurrentEnvironment())
            mFirebaseAnalytics?.setUserProperty("app_version", getAppVersion(context))

            // Enable/disable analytics based on environment
            if (isProduction()) {
                mFirebaseAnalytics?.setAnalyticsCollectionEnabled(true)
                Log.d(TAG, "Analytics enabled for production")
            } else {
                mFirebaseAnalytics?.setAnalyticsCollectionEnabled(false)
                Log.d(TAG, "Analytics disabled for non-production")
            }

            Log.d(TAG, "Firebase Analytics initialized ✅")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Analytics: " + e.message)
        }
    }

    /**
     * Initialize Firebase Cloud Messaging
     */
    @JvmStatic
    private fun initializeFCM() {
        try {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Log.d(TAG, "FCM Token: $token")
                        // Store token in Constants or SharedPreferences
                        Constants.FCM_TOKEN = token
                    } else {
                        Log.e(TAG, "FCM Token retrieval failed: " + task.exception)
                    }
                }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FCM: " + e.message)
        }
    }

    /**
     * Get Firebase API Key based on current environment
     */
    private fun getFirebaseApiKey(): String {
        return if (isProduction()) {
            "AIzaSyAuTnhWZ10pKLXEbOz2Dqys5oHe90ZLeeI"
        } else {
            "AIzaSyD-JgLZbNTm0H_Jm2OriDo-EmLM8HMfJXo"
        }
    }

    /**
     * Get Firebase Project ID based on current environment
     */
    private fun getFirebaseProjectId(): String {
        return if (isProduction()) {
            "lawyer-tracking-dev"
        } else {
            "testpushnotifications-fb862"
        }
    }

    /**
     * Get Firebase App ID based on current environment
     */
    private fun getFirebaseAppId(): String {
        return if (isProduction()) {
            "1:162523675998:android:5c9be24ff92ac4d3aee3cf"
        } else {
            "1:150510681156:android:4843438eb4ee1dc18d9011"
        }
    }

    /**
     * Get Firebase Database URL based on current environment
     */
    private fun getFirebaseDatabaseUrl(): String {
        return if (isProduction()) {
            "https://lawyer-tracking-dev.firebaseio.com"
        } else {
            "https://testpushnotifications-fb862.firebaseio.com"
        }
    }

    /**
     * Get Firebase Storage Bucket based on current environment
     */
    private fun getFirebaseStorageBucket(): String {
        return if (isProduction()) {
            "lawyer-tracking-dev.firebasestorage.app"
        } else {
            "testpushnotifications-fb862.firebasestorage.app"
        }
    }

    /**
     * Check if current build is production using Constants manual environment flags
     */
    private fun isProduction(): Boolean {
        return Constants.ISPRODUCTION
    }

    /**
     * Check if current build is staging using Constants manual environment flags
     */
    private fun isStaging(): Boolean {
        return Constants.IS_STAGING
    }

    /**
     * Get current environment name
     */
    @JvmStatic
    fun getCurrentEnvironment(): String {
        return if (isProduction()) {
            "PRODUCTION"
        } else if (isStaging()) {
            "STAGING"
        } else {
            "DEV"
        }
    }

    /**
     * Get app version from PackageManager
     */
    private fun getAppVersion(context: Context): String {
        return try {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    /**
     * Get Firebase Analytics instance
     */
    @JvmStatic
    fun getAnalytics(): FirebaseAnalytics? {
        return mFirebaseAnalytics
    }

    /**
     * Log event with single parameter (3 arguments)
     */
    @JvmStatic
    fun logEvent(eventName: String, paramKey: String, paramValue: String) {
        try {
            if (mFirebaseAnalytics != null) {
                val params = Bundle()
                params.putString(paramKey, paramValue)
                mFirebaseAnalytics?.logEvent(eventName, params)
                Log.d(TAG, "Event logged: $eventName | $paramKey=$paramValue")
            } else {
                Log.w(TAG, "Firebase Analytics not initialized, event not logged: $eventName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log event: " + e.message)
        }
    }

    /**
     * Log event with Bundle parameters
     */
    @JvmStatic
    fun logEvent(eventName: String, params: Bundle) {
        try {
            if (mFirebaseAnalytics != null) {
                mFirebaseAnalytics?.logEvent(eventName, params)
                Log.d(TAG, "Event logged: $eventName with params")
            } else {
                Log.w(TAG, "Firebase Analytics not initialized, event not logged: $eventName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log event: " + e.message)
        }
    }

    /**
     * Log event with multiple parameters (varargs)
     */
    @JvmStatic
    fun logEvent(eventName: String, vararg params: String) {
        try {
            if (mFirebaseAnalytics != null) {
                if (params.size % 2 != 0) {
                    Log.w(TAG, "Params must be in key-value pairs")
                    return
                }

                val bundle = Bundle()
                var i = 0
                while (i < params.size) {
                    bundle.putString(params[i], params[i + 1])
                    i += 2
                }
                mFirebaseAnalytics?.logEvent(eventName, bundle)
                Log.d(TAG, "Event logged: $eventName with ${params.size / 2} parameters")
            } else {
                Log.w(TAG, "Firebase Analytics not initialized, event not logged: $eventName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log event: " + e.message)
        }
    }

    /**
     * Log user property to Firebase Analytics
     */
    @JvmStatic
    fun setUserProperty(propertyName: String, value: String) {
        try {
            if (mFirebaseAnalytics != null) {
                mFirebaseAnalytics?.setUserProperty(propertyName, value)
                Log.d(TAG, "User property set: $propertyName = $value")
            } else {
                Log.w(TAG, "Firebase Analytics not initialized, user property not set: $propertyName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set user property: " + e.message)
        }
    }
}
