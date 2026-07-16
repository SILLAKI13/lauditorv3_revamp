package com.digicoffer.lauditor.CommonFiles.ChatService

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.Nullable
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import java.io.IOException

class ChatConnectionService : Service {

    private var mActive = false
    private var mThread: Thread? = null
    private var mTHandler: Handler? = null
    private var retryCount = 0
    private val mainHandler = Handler(Looper.getMainLooper())

    constructor()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")
    }

    private fun initConnection() {
        Log.d(TAG, "initConnection() — attempt " + (retryCount + 1) + " of " + MAX_RETRY_ATTEMPTS)

        if (mConnection == null) {
            mConnection = ChatConnection(this)
        }

        try {
            sConnectionState = ChatConnection.ConnectionState.CONNECTING
            Log.d(TAG, "initConnection() — state set to CONNECTING")

            mConnection!!.connect()

            sConnectionState = ChatConnection.ConnectionState.CONNECTED
            Log.d(TAG, "initConnection() — state set to CONNECTED")

            retryCount = 0
        } catch (e: Exception) {
            Log.e(TAG, "initConnection() — connect failed: " + e.message)
            e.fillInStackTrace()

            sConnectionState = ChatConnection.ConnectionState.DISCONNECTED
            Log.d(TAG, "initConnection() — state set to DISCONNECTED after failure")

            scheduleRetry()
        }
    }

    private fun scheduleRetry() {
        retryCount++

        if (retryCount > MAX_RETRY_ATTEMPTS) {
            Log.e(TAG, "scheduleRetry() — max retries ($MAX_RETRY_ATTEMPTS) reached. Stopping service.")
            stopSelf()
            return
        }

        val delayMs = RETRY_DELAY_MS * retryCount
        Log.d(TAG, "scheduleRetry() — scheduling retry #$retryCount in ${delayMs}ms")

        mainHandler.postDelayed({
            if (mActive) {
                Log.d(TAG, "scheduleRetry() — executing retry #$retryCount")
                Thread {
                    Looper.prepare()
                    initConnection()
                    Looper.loop()
                }.start()
            } else {
                Log.d(TAG, "scheduleRetry() — service inactive, skipping retry #$retryCount")
            }
        }, delayMs)
    }

    fun start() {
        Log.d(TAG, "start() — Service Start() function called.")
        if (!mActive) {
            mActive = true
            retryCount = 0

            if (mThread == null || mThread!!.isAlive == false) {
                mThread = Thread {
                    Looper.prepare()
                    mTHandler = Handler()
                    initConnection()
                    Looper.loop()
                }
                mThread!!.start()
            }
        }
    }

    fun stop() {
        Log.d(TAG, "stop()")
        mActive = false

        mainHandler.removeCallbacksAndMessages(null)

        if (mTHandler != null) {
            mTHandler!!.post {
                if (mConnection != null) {
                    mConnection!!.disconnect()
                    sConnectionState = ChatConnection.ConnectionState.DISCONNECTED
                    Log.d(TAG, "stop() — connection disconnected, state set to DISCONNECTED")
                }
            }
        } else {
            Log.d(TAG, "stop() — mTHandler is null, skipping disconnect post")
            if (mConnection != null) {
                sConnectionState = ChatConnection.ConnectionState.DISCONNECTED
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")
        start()
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
        stop()
    }

    companion object {
        private const val TAG = "RoosterService"

        const val UI_AUTHENTICATED = "com.digisec.digicoffer.uiauthenticated"
        const val SEND_MESSAGE = "com.digisec.digicoffer.sendmessage"
        const val BUNDLE_MESSAGE_BODY = "b_body"
        const val BUNDLE_MESSAGE_SUBJECT = "b_subject"
        const val BUNDLE_TO = "b_to"
        const val NEW_MESSAGE = "com.digisec.digicoffer.newmessage"
        const val BUNDLE_FROM_JID = "b_from"

        private const val MAX_RETRY_ATTEMPTS = 5
        private const val RETRY_DELAY_MS = 3000L

        @JvmField
        var sConnectionState: ChatConnection.ConnectionState? = null

        @JvmField
        var sLoggedInState: ChatConnection.LoggedInState? = null

        @JvmField
        var mConnection: ChatConnection? = null

        @JvmStatic
        fun getState(): ChatConnection.ConnectionState {
            return sConnectionState ?: ChatConnection.ConnectionState.DISCONNECTED
        }

        @JvmStatic
        fun getLoggedInState(): ChatConnection.LoggedInState {
            return sLoggedInState ?: ChatConnection.LoggedInState.LOGGED_OUT
        }
    }
}
