package com.cse2200.maacare.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import com.cse2200.maacare.data.db.MaaCareDatabase;
import com.cse2200.maacare.data.db.entity.HydrationLogEntity;
import com.cse2200.maacare.data.prefs.PreferenceManager;
import com.cse2200.maacare.util.NotificationHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Foreground Service running an ongoing maternal hydration session.
 * Fully synchronized with Room database and offers notification action buttons.
 */
public class MaternalHealthMonitorService extends Service {

    public static final String ACTION_START = "com.cse2200.maacare.action.START_MONITOR";
    public static final String ACTION_STOP = "com.cse2200.maacare.action.STOP_MONITOR";
    public static final String ACTION_LOG_WATER_250 = "com.cse2200.maacare.action.LOG_WATER_250";
    public static final String ACTION_LOG_WATER_500 = "com.cse2200.maacare.action.LOG_WATER_500";
    public static final String ACTION_DECREMENT_250 = "com.cse2200.maacare.action.DECREMENT_250";

    public static final String BROADCAST_WATER_UPDATED = "com.cse2200.maacare.broadcast.WATER_UPDATED";
    public static final String EXTRA_WATER_COUNT = "extra_water_count";

    private static final String TAG = "HealthMonitorService";
    private PreferenceManager preferenceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        preferenceManager = new PreferenceManager(this);
        NotificationHelper.createNotificationChannels(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                startForegroundSession();
            } else if (ACTION_STOP.equals(action)) {
                stopForegroundSession();
            } else if (ACTION_LOG_WATER_250.equals(action)) {
                logIntakeVolume(250);
            } else if (ACTION_LOG_WATER_500.equals(action)) {
                logIntakeVolume(500);
            } else if (ACTION_DECREMENT_250.equals(action)) {
                logIntakeVolume(-250);
            }
        } else {
            startForegroundSession();
        }
        return START_STICKY;
    }

    private void startForegroundSession() {
        preferenceManager.setMonitorActive(true);
        int current = preferenceManager.getTodayWaterIntakeMl();
        int goal = preferenceManager.getDailyWaterGoalMl();

        Notification notification = NotificationHelper
                .buildForegroundMonitorNotification(this, current, goal)
                .build();
        startForeground(NotificationHelper.NOTIFICATION_MONITOR_ID, notification);
    }

    private void logIntakeVolume(int deltaMl) {
        preferenceManager.adjustTodayWaterIntakeMl(deltaMl);
        int updatedTotal = preferenceManager.getTodayWaterIntakeMl();
        int goal = preferenceManager.getDailyWaterGoalMl();

        // Persist to Room Database asynchronously
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            MaaCareDatabase db = MaaCareDatabase.getInstance(getApplicationContext());
            db.hydrationDao().insert(new HydrationLogEntity(todayDate, deltaMl, System.currentTimeMillis()));
        });

        // Update ongoing foreground notification
        Notification notification = NotificationHelper
                .buildForegroundMonitorNotification(this, updatedTotal, goal)
                .build();
        NotificationManagerCompat.from(this).notify(NotificationHelper.NOTIFICATION_MONITOR_ID, notification);

        // Broadcast to notify visible Activities/Fragments
        Intent broadcast = new Intent(BROADCAST_WATER_UPDATED);
        broadcast.putExtra(EXTRA_WATER_COUNT, updatedTotal);
        sendBroadcast(broadcast);
    }

    private void stopForegroundSession() {
        preferenceManager.setMonitorActive(false);
        stopForeground(true);
        stopSelf();
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
