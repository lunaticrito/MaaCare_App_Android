package com.cse2200.maacare.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.cse2200.maacare.data.db.MaaCareDatabase;
import com.cse2200.maacare.data.db.entity.MedicineEntity;
import com.cse2200.maacare.util.AlarmScheduler;
import java.util.List;

/**
 * BroadcastReceiver triggered on system reboot (RECEIVE_BOOT_COMPLETED)
 * to re-arm all active medication alarms from the local database.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device rebooted. Rescheduling all active maternal medicine reminders...");
            MaaCareDatabase.databaseWriteExecutor.execute(() -> {
                MaaCareDatabase db = MaaCareDatabase.getInstance(context);
                List<MedicineEntity> activeList = db.medicineDao().getActiveReminders();
                for (MedicineEntity medicine : activeList) {
                    AlarmScheduler.scheduleMedicationAlarm(context, medicine);
                }
                Log.d(TAG, "Successfully restored " + activeList.size() + " medication alarms.");
            });
        }
    }
}
