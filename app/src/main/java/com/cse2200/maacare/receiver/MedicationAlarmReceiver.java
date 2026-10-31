package com.cse2200.maacare.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.cse2200.maacare.R;
import com.cse2200.maacare.data.db.MaaCareDatabase;
import com.cse2200.maacare.data.db.entity.MedicineEntity;
import com.cse2200.maacare.ui.MainActivity;
import com.cse2200.maacare.util.AlarmScheduler;
import com.cse2200.maacare.util.NotificationHelper;

/**
 * BroadcastReceiver triggered by AlarmManager to post high-priority medication notifications.
 */
public class MedicationAlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_MED_ID = "extra_medicine_id";
    public static final String EXTRA_MED_NAME = "extra_medicine_name";
    public static final String EXTRA_MED_DOSAGE = "extra_medicine_dosage";

    private static final String TAG = "MedicationAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        long medId = intent.getLongExtra(EXTRA_MED_ID, 0);
        String medName = intent.getStringExtra(EXTRA_MED_NAME);
        String dosage = intent.getStringExtra(EXTRA_MED_DOSAGE);

        if (medName == null) medName = "Prescribed Supplement";
        if (dosage == null) dosage = "Take as prescribed by obstetrician";

        NotificationHelper.createNotificationChannels(context);

        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) medId, openAppIntent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_MEDICATION_ID)
                .setSmallIcon(R.drawable.ic_medication)
                .setContentTitle("Maternal Medication Reminder: " + medName)
                .setContentText(dosage)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        "Time to take your maternal supplement: " + medName + " (" + dosage + "). Staying consistent ensures vital fetal organ growth."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify((int) (1000 + medId), builder.build());
            Log.d(TAG, "Fired medication alert notification for: " + medName);
        } catch (SecurityException se) {
            Log.e(TAG, "Notification permission missing on Android 13+", se);
        }

        // Reschedule alarm for next day's dosage cycle
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            MaaCareDatabase db = MaaCareDatabase.getInstance(context);
            MedicineEntity entity = db.medicineDao().getMedicineById(medId);
            if (entity != null && entity.isReminderEnabled()) {
                AlarmScheduler.scheduleMedicationAlarm(context, entity);
            }
        });
    }
}
