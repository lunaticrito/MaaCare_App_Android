package com.cse2200.maacare.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.cse2200.maacare.data.db.entity.MedicineEntity;
import com.cse2200.maacare.receiver.MedicationAlarmReceiver;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Manages exact and repeating alarm scheduling via Android AlarmManager.
 */
public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";

    public static void scheduleMedicationAlarm(Context context, MedicineEntity medicine) {
        if (!medicine.isReminderEnabled()) {
            cancelMedicationAlarm(context, medicine);
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, MedicationAlarmReceiver.class);
        intent.putExtra(MedicationAlarmReceiver.EXTRA_MED_ID, medicine.getId());
        intent.putExtra(MedicationAlarmReceiver.EXTRA_MED_NAME, medicine.getName());
        intent.putExtra(MedicationAlarmReceiver.EXTRA_MED_DOSAGE, medicine.getDosage());

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) medicine.getId(),
                intent,
                flags
        );

        Calendar targetTime = parseTimeStringToCalendar(medicine.getReminderTime());

        // If target time has already passed today, schedule for next day
        if (targetTime.before(Calendar.getInstance())) {
            targetTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        long triggerAtMillis = targetTime.getTimeInMillis();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
            Log.d(TAG, "Scheduled alarm for " + medicine.getName() + " at: " + targetTime.getTime());
        } catch (SecurityException se) {
            // Fallback for Android 12+ if exact alarm permission is restricted
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            Log.w(TAG, "Exact alarm permission restricted, scheduled inexact alarm.", se);
        }
    }

    public static void cancelMedicationAlarm(Context context, MedicineEntity medicine) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, MedicationAlarmReceiver.class);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) medicine.getId(),
                intent,
                flags
        );
        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "Cancelled alarm for " + medicine.getName());
    }

    private static Calendar parseTimeStringToCalendar(String timeStr) {
        Calendar cal = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = sdf.parse(timeStr);
            if (date != null) {
                Calendar parsed = Calendar.getInstance();
                parsed.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, parsed.get(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE, parsed.get(Calendar.MINUTE));
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            }
        } catch (Exception e) {
            // Default 9:00 AM fallback
            cal.set(Calendar.HOUR_OF_DAY, 9);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
        }
        return cal;
    }
}
