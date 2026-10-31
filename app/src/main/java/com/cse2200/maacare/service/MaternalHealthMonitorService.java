package com.cse2200.maacare.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import com.cse2200.maacare.data.prefs.PreferenceManager;
import com.cse2200.maacare.util.NotificationHelper;

/**
 * Foreground Service maintaining a persistent maternal hydration and health session.
 * Exposes a notification action button to record fluid intake without opening the app.
 */
public class MaternalHealthMonitorService extends Service {

    public static final String ACTION_START = "com.cse2200.maacare.action.START_MONITOR";
    public static final String ACTION_STOP = "com.cse2200.maacare.action.STOP_MONITOR";
    public static final String ACTION_LOG_WATER = "com.cse2200.maacare.action.LOG_WATER";

    public static final String BROADCAST_WATER_UPDATED = "com.cse2200.maacare.broadcast.WATER_UPDATED";
    public static final String EXTRA_WATER_COUNT = "extra_water_count";

    private static final String TAG = "HealthMonitorService";
    private PreferenceManager preferenceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        preferenceManager = new PreferenceManager(this);
        NotificationHelper.createNotificationChannels(this);
        Log.d(TAG, "MaternalHealthMonitorService created.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                startForegroundServiceSession();
            } else if (ACTION_STOP.equals(action)) {
                stopForegroundServiceSession();
            } else if (ACTION_LOG_WATER.equals(action)) {
                handleWaterIntakeAction();
            }
        } else {
            startForegroundServiceSession();
        }
        return START_STICKY;
    }

    private void startForegroundServiceSession() {
        preferenceManager.setMonitorActive(true);
        int currentWater = preferenceManager.getWaterGlasses();
        Notification notification = NotificationHelper
                .buildForegroundMonitorNotification(this, currentWater)
                .build();
        startForeground(NotificationHelper.NOTIFICATION_MONITOR_ID, notification);
        Log.d(TAG, "Started foreground maternal monitor notification.");
    }

    private void handleWaterIntakeAction() {
        preferenceManager.incrementWaterGlasses();
        int updatedCount = preferenceManager.getWaterGlasses();

        // Update active foreground notification
        Notification notification = NotificationHelper
                .buildForegroundMonitorNotification(this, updatedCount)
                .build();
        NotificationManagerCompat.from(this).notify(NotificationHelper.NOTIFICATION_MONITOR_ID, notification);

        // Broadcast change so active UI activities/fragments update immediately
        Intent broadcast = new Intent(BROADCAST_WATER_UPDATED);
        broadcast.putExtra(EXTRA_WATER_COUNT, updatedCount);
        sendBroadcast(broadcast);

        Log.d(TAG, "Logged water from notification action. New count: " + updatedCount);
    }

    private void stopForegroundServiceSession() {
        preferenceManager.setMonitorActive(false);
        stopForeground(true);
        stopSelf();
        Log.d(TAG, "Stopped maternal monitor service.");
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, MaternalHealthMonitorService.class);
        intent.setAction(ACTION_START);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, MaternalHealthMonitorService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
