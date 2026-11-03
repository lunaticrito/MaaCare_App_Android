package com.cse2200.maacare.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.cse2200.maacare.R;
import com.cse2200.maacare.service.MaternalHealthMonitorService;
import com.cse2200.maacare.ui.MainActivity;

/**
 * Handles creation of High-Priority Notification Channels and NotificationCompat Builders
 * for medication alerts and the live foreground maternal hydration monitor.
 */
public class NotificationHelper {

    public static final String CHANNEL_MEDICATION_ID = "channel_maacare_medications";
    public static final String CHANNEL_MEDICATION_NAME = "Prescribed Medication Alerts";

    public static final String CHANNEL_MONITOR_ID = "channel_maacare_monitor";
    public static final String CHANNEL_MONITOR_NAME = "Live Maternal Health Monitor";

    public static final int NOTIFICATION_MONITOR_ID = 1001;

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            // High-importance channel with vibration pattern for medication alerts
            NotificationChannel medChannel = new NotificationChannel(
                    CHANNEL_MEDICATION_ID,
                    CHANNEL_MEDICATION_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            medChannel.setDescription("High priority alarms for maternal prescribed medicine & supplements.");
            medChannel.enableLights(true);
            medChannel.setLightColor(Color.MAGENTA);
            medChannel.enableVibration(true);
            medChannel.setVibrationPattern(new long[]{0, 500, 200, 500});
            manager.createNotificationChannel(medChannel);

            // Channel for Foreground Maternal Health Monitoring
            NotificationChannel monitorChannel = new NotificationChannel(
                    CHANNEL_MONITOR_ID,
                    CHANNEL_MONITOR_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            monitorChannel.setDescription("Persistent foreground notification for active maternal hydration & health monitoring.");
            manager.createNotificationChannel(monitorChannel);
        }
    }

    /**
     * Builds persistent foreground notification with '+250 mL' and '+500 mL' quick action buttons.
     */
    public static NotificationCompat.Builder buildForegroundMonitorNotification(Context context, int currentMl, int goalMl) {
        Intent launchIntent = new Intent(context, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 0, launchIntent, flags);

        // PendingIntent for Action Button: +250 mL
        Intent add250Intent = new Intent(context, MaternalHealthMonitorService.class);
        add250Intent.setAction(MaternalHealthMonitorService.ACTION_LOG_WATER_250);
        PendingIntent add250Pending = PendingIntent.getService(context, 1, add250Intent, flags);

        // PendingIntent for Action Button: +500 mL
        Intent add500Intent = new Intent(context, MaternalHealthMonitorService.class);
        add500Intent.setAction(MaternalHealthMonitorService.ACTION_LOG_WATER_500);
        PendingIntent add500Pending = PendingIntent.getService(context, 2, add500Intent, flags);

        int percent = goalMl > 0 ? (int) (((double) currentMl / goalMl) * 100) : 0;

        return new NotificationCompat.Builder(context, CHANNEL_MONITOR_ID)
                .setSmallIcon(R.drawable.ic_water_drop)
                .setContentTitle("MaaCare • Maternal Hydration Active")
                .setContentText("Intake: " + currentMl + " mL / " + goalMl + " mL (" + percent + "%)")
                .setContentIntent(contentPendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(R.drawable.ic_water_drop, "+250 mL", add250Pending)
                .addAction(R.drawable.ic_water_drop, "+500 mL", add500Pending);
    }
}
