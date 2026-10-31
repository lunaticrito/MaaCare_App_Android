package com.cse2200.maacare.ui;

import android.Manifest;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.cse2200.maacare.R;
import com.cse2200.maacare.databinding.ActivityMainBinding;
import com.cse2200.maacare.receiver.NetworkStateChangeReceiver;
import com.cse2200.maacare.ui.fragments.CareAndVaccineFragment;
import com.cse2200.maacare.ui.fragments.EmergencyGuideFragment;
import com.cse2200.maacare.ui.fragments.HomeTrackerFragment;
import com.cse2200.maacare.ui.fragments.MedicineReminderFragment;
import com.cse2200.maacare.util.NotificationHelper;

/**
 * Main Activity hosting BottomNavigationView, Fragment container,
 * dynamic ConnectivityManager BroadcastReceiver, and runtime permission requests.
 */
public class MainActivity extends AppCompatActivity implements NetworkStateChangeReceiver.NetworkStateListener {

    private static final int PERMISSION_REQUEST_NOTIFICATIONS = 101;

    private ActivityMainBinding binding;
    private NetworkStateChangeReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Notification Channels
        NotificationHelper.createNotificationChannels(this);

        // Request Android 13 POST_NOTIFICATIONS runtime permission if applicable
        requestNotificationPermissionIfNeeded();

        // Setup Bottom Navigation
        setupBottomNavigation();

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeTrackerFragment());
        }

        // Register Dynamic BroadcastReceiver for Connectivity
        networkReceiver = new NetworkStateChangeReceiver(this);
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeTrackerFragment();
            } else if (itemId == R.id.navigation_medicine) {
                selectedFragment = new MedicineReminderFragment();
            } else if (itemId == R.id.navigation_care_vaccine) {
                selectedFragment = new CareAndVaccineFragment();
            } else if (itemId == R.id.navigation_emergency) {
                selectedFragment = new EmergencyGuideFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_NOTIFICATIONS
                );
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(networkReceiver);
        } catch (IllegalArgumentException ignored) {}
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        runOnUiThread(() -> {
            if (isConnected) {
                binding.tvOfflineBanner.setVisibility(View.GONE);
            } else {
                binding.tvOfflineBanner.setVisibility(View.VISIBLE);
            }
        });
    }
}
