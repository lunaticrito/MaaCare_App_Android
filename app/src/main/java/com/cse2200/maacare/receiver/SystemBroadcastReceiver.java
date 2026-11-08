package com.cse2200.maacare.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

/**
 * Dynamic system broadcast receiver monitoring critical environment states:
 * 1. Airplane Mode Changed (switches between Offline Emergency and Cloud Sync modes)
 * 2. Battery Low (prompts immediate automated caregiver alert dispatch)
 */
public class SystemBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "SystemBroadcastReceiver";

    public interface SystemBroadcastListener {
        void onAirplaneModeChanged(boolean isAirplaneModeOn);
        void onBatteryLow();
    }

    private final SystemBroadcastListener listener;

    public SystemBroadcastReceiver(SystemBroadcastListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        String action = intent.getAction();

        if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            boolean isAirplaneModeOn;
            try {
                isAirplaneModeOn = Settings.Global.getInt(
                        context.getContentResolver(),
                        Settings.Global.AIRPLANE_MODE_ON, 0
                ) != 0;
            } catch (Exception e) {
                isAirplaneModeOn = intent.getBooleanExtra("state", false);
            }
            Log.d(TAG, "Airplane mode changed: " + isAirplaneModeOn);
            if (listener != null) {
                listener.onAirplaneModeChanged(isAirplaneModeOn);
            }
        } else if (Intent.ACTION_BATTERY_LOW.equals(action)) {
            Log.d(TAG, "System battery low alert received.");
            if (listener != null) {
                listener.onBatteryLow();
            }
        }
    }
}
