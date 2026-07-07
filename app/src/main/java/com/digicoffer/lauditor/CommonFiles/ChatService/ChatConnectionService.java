package com.digicoffer.lauditor.CommonFiles.ChatService;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

public class ChatConnectionService extends Service {
    private static final String TAG = "RoosterService";

    public static final String UI_AUTHENTICATED = "com.digisec.digicoffer.uiauthenticated";
    public static final String SEND_MESSAGE = "com.digisec.digicoffer.sendmessage";
    public static final String BUNDLE_MESSAGE_BODY = "b_body";
    public static final String BUNDLE_MESSAGE_SUBJECT = "b_subject";
    public static final String BUNDLE_TO = "b_to";
    public static final String NEW_MESSAGE = "com.digisec.digicoffer.newmessage";
    public static final String BUNDLE_FROM_JID = "b_from";
    public static ChatConnection.ConnectionState sConnectionState;
    public static ChatConnection.LoggedInState sLoggedInState;
    private boolean mActive;
    private Thread mThread;
    private Handler mTHandler;
    private static ChatConnection mConnection;

    // ✅ FIX: Maximum retry attempts to prevent infinite retry loops
    private static final int MAX_RETRY_ATTEMPTS = 5;

    // ✅ FIX: Delay in milliseconds between each retry attempt (increases with each retry)
    private static final long RETRY_DELAY_MS = 3000;

    // ✅ FIX: Track how many reconnect attempts have been made
    private int retryCount = 0;

    // ✅ FIX: Handler on the main thread used to post delayed retry attempts
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ChatConnectionService() {
    }

    public static ChatConnection.ConnectionState getState() {
        if (sConnectionState == null) {
            // ✅ FIX: sConnectionState is null on first call because it is only assigned
            //         inside ChatConnection callbacks (onConnected / onDisconnected).
            //         Returning DISCONNECTED here is correct — do NOT change this to CONNECTED,
            //         as that would mask the real disconnected state and skip reconnect logic.
            return ChatConnection.ConnectionState.DISCONNECTED;
        }
        return sConnectionState;
    }

    public static ChatConnection.LoggedInState getLoggedInState() {
        if (sLoggedInState == null) {
            return ChatConnection.LoggedInState.LOGGED_OUT;
        }
        return sLoggedInState;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
    }

    private void initConnection() {
        Log.d(TAG, "initConnection() — attempt " + (retryCount + 1) + " of " + MAX_RETRY_ATTEMPTS);

        // ✅ FIX: Create mConnection once and reuse it.
        //         Previously a new ChatConnection was created every retry, leaking resources.
        if (mConnection == null) {
            mConnection = new ChatConnection(this);
        }

        try {
            // ✅ FIX: sConnectionState is set to CONNECTING before the attempt so that
            //         getState() never returns stale DISCONNECTED while a connect is in flight.
            //         ChatConnection.onConnected() must set sConnectionState = CONNECTED.
            //         ChatConnection.onDisconnected() must set sConnectionState = DISCONNECTED.
            //         If those callbacks are missing in your ChatConnection class, add them —
            //         that is why the state was always reading DISCONNECTED.
            sConnectionState = ChatConnection.ConnectionState.CONNECTING;
            Log.d(TAG, "initConnection() — state set to CONNECTING");

            mConnection.connect();

            // ✅ FIX: If connect() returns without throwing, the connection succeeded.
            //         Set state to CONNECTED here as a safety net in case ChatConnection
            //         does not have an onConnected() callback that sets this.
            sConnectionState = ChatConnection.ConnectionState.CONNECTED;
            Log.d(TAG, "initConnection() — state set to CONNECTED");

            // ✅ FIX: Reset retry counter on successful connection
            retryCount = 0;

        } catch (IOException | SmackException | XMPPException e) {
            Log.e(TAG, "initConnection() — connect failed: " + e.getMessage());
            e.fillInStackTrace();

            // ✅ FIX: Set state to DISCONNECTED on failure so callers get the correct state
            sConnectionState = ChatConnection.ConnectionState.DISCONNECTED;
            Log.d(TAG, "initConnection() — state set to DISCONNECTED after failure");

            // ✅ FIX: Schedule a retry with exponential backoff instead of calling stopSelf().
            //         Previously the service stopped itself on the first failure, meaning
            //         the connection was never retried and stayed DISCONNECTED permanently.
            scheduleRetry();
        }
    }

    /**
     * ✅ FIX: Schedules a reconnect attempt on a background thread with a delay.
     *
     * Previously the service called stopSelf() on any connection error, which meant
     * the XMPP connection was permanently broken until the user killed and restarted
     * the app. Now we retry up to MAX_RETRY_ATTEMPTS times with increasing delays.
     *
     * Delay schedule:
     *   Attempt 1: 3s
     *   Attempt 2: 6s
     *   Attempt 3: 9s
     *   Attempt 4: 12s
     *   Attempt 5: 15s
     *   After 5 failures: stop retrying and call stopSelf()
     */
    private void scheduleRetry() {
        retryCount++;

        if (retryCount > MAX_RETRY_ATTEMPTS) {
            Log.e(TAG, "scheduleRetry() — max retries (" + MAX_RETRY_ATTEMPTS + ") reached. Stopping service.");
            stopSelf();
            return;
        }

        long delayMs = RETRY_DELAY_MS * retryCount;
        Log.d(TAG, "scheduleRetry() — scheduling retry #" + retryCount + " in " + delayMs + "ms");

        mainHandler.postDelayed(() -> {
            // ✅ FIX: Only retry if the service is still active (not stopped between retries)
            if (mActive) {
                Log.d(TAG, "scheduleRetry() — executing retry #" + retryCount);
                // ✅ FIX: Run reconnect on a background thread — network on main thread crashes
                new Thread(() -> {
                    Looper.prepare();
                    initConnection();
                    Looper.loop();
                }).start();
            } else {
                Log.d(TAG, "scheduleRetry() — service inactive, skipping retry #" + retryCount);
            }
        }, delayMs);
    }

    public void start() {
        Log.d(TAG, "start() — Service Start() function called.");
        if (!mActive) {
            mActive = true;
            // ✅ FIX: Reset retry count when the service is intentionally started fresh
            retryCount = 0;

            if (mThread == null || !mThread.isAlive()) {
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        mTHandler = new Handler();
                        initConnection();
                        // THE CODE HERE RUNS IN A BACKGROUND THREAD.
                        Looper.loop();
                    }
                });
                mThread.start();
            }
        }
    }

    public void stop() {
        Log.d(TAG, "stop()");
        mActive = false;

        // ✅ FIX: Cancel any pending retry callbacks when the service is stopped
        //         so we don't attempt reconnects after explicit stop/destroy
        mainHandler.removeCallbacksAndMessages(null);

        // ✅ FIX: Guard against mTHandler being null if stop() is called before
        //         start() completes (race condition on rapid create/destroy)
        if (mTHandler != null) {
            mTHandler.post(() -> {
                if (mConnection != null) {
                    mConnection.disconnect();
                    // ✅ FIX: Set state to DISCONNECTED when explicitly stopped
                    sConnectionState = ChatConnection.ConnectionState.DISCONNECTED;
                    Log.d(TAG, "stop() — connection disconnected, state set to DISCONNECTED");
                }
            });
        } else {
            Log.d(TAG, "stop() — mTHandler is null, skipping disconnect post");
            if (mConnection != null) {
                // ✅ FIX: Still update the state even if handler is gone
                sConnectionState = ChatConnection.ConnectionState.DISCONNECTED;
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        start();
        return Service.START_STICKY;
        // RETURNING START_STICKY CAUSES OUR CODE TO STICK AROUND WHEN THE APP ACTIVITY HAS DIED.
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
        stop();
    }
}