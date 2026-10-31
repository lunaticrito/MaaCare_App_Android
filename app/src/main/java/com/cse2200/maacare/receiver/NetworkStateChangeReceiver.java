package com.cse2200.maacare.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Dynamic BroadcastReceiver for ConnectivityManager changes.
 * Alerts user when offline emergency mode is active.
 */
public class NetworkStateChangeReceiver extends BroadcastReceiver {

    public interface NetworkStateListener {
        void onNetworkChanged(boolean isConnected);
    }

    private final NetworkStateListener listener;

    public NetworkStateChangeReceiver(NetworkStateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (listener != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean isConnected = false;
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                isConnected = (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
            }
            listener.onNetworkChanged(isConnected);
        }
    }
}
