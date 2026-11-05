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
import com.cse2200.maacare.data.prefs.PreferenceManager;
import com.cse2200.maacare.databinding.ActivityMainBinding;
import com.cse2200.maacare.receiver.NetworkStateChangeReceiver;
import com.cse2200.maacare.ui.auth.LoginActivity;
import com.cse2200.maacare.ui.fragments.CareAndVaccineFragment;
import com.cse2200.maacare.ui.fragments.EmergencyGuideFragment;
import com.cse2200.maacare.ui.fragments.HomeTrackerFragment;
import com.cse2200.maacare.ui.fragments.MedicineReminderFragment;
import com.cse2200.maacare.util.NotificationHelper;
import com.cse2200.maacare.data.repository.MaaCareRepository;
import com.cse2200.maacare.data.db.entity.BabyCareTimelineEntity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.content.Intent;

/**
 * Main Activity hosting BottomNavigationView, Fragment container,
 * dynamic ConnectivityManager BroadcastReceiver, and runtime permission requests.
 */
public class MainActivity extends BaseActivity implements NetworkStateChangeReceiver.NetworkStateListener {

    private static final int PERMISSION_REQUEST_NOTIFICATIONS = 101;

    private ActivityMainBinding binding;
    private NetworkStateChangeReceiver networkReceiver;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager = new PreferenceManager(this);
        if (!preferenceManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Notification Channels
        NotificationHelper.createNotificationChannels(this);

        // Request Android 13 POST_NOTIFICATIONS runtime permission if applicable
        requestNotificationPermissionIfNeeded();

        // Top App Bar Controls (Theme & Language Switchers)
        setupTopBarControls();

        // Setup Bottom Navigation & Center FAB
        setupBottomNavigation();

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeTrackerFragment());
        }

        // Register Dynamic BroadcastReceiver for Connectivity
        networkReceiver = new NetworkStateChangeReceiver(this);
    }

    private void setupTopBarControls() {
        binding.btnThemeToggle.setOnClickListener(v -> toggleTheme());
        binding.btnLanguageToggle.setOnClickListener(v -> toggleLanguage());
        binding.fabCenterQuickLog.setOnClickListener(v -> showQuickLogBottomSheet());
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeTrackerFragment();
            } else if (itemId == R.id.navigation_medicine) {
                selectedFragment = new MedicineReminderFragment();
            } else if (itemId == R.id.navigation_quick_log) {
                showQuickLogBottomSheet();
                return false;
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

    private void showQuickLogBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_quick_log, null);
        dialog.setContentView(view);

        MaaCareRepository repo = new MaaCareRepository(this);
        long now = System.currentTimeMillis();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(now));
        String timeStr = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(now));

        view.findViewById(R.id.btn_quick_water).setOnClickListener(v -> {
            preferenceManager.adjustTodayWaterIntakeMl(250);
            Toast.makeText(this, "+250 mL logged! Stay healthy & hydrated.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            refreshCurrentFragment();
        });

        view.findViewById(R.id.btn_quick_feeding).setOnClickListener(v -> {
            BabyCareTimelineEntity feed = new BabyCareTimelineEntity(
                    dateStr, timeStr, now, "FEEDING", "Baby Feeding Recorded",
                    "Logged via 1-tap quick action", "#E6F2FF"
            );
            repo.insertBabyEvent(feed, new MaaCareRepository.DataCallback<Long>() {
                @Override public void onSuccess(Long result) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Feeding session recorded!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        refreshCurrentFragment();
                    });
                }
                @Override public void onError(Throwable error) {}
            });
        });

        view.findViewById(R.id.btn_quick_diaper).setOnClickListener(v -> {
            BabyCareTimelineEntity diaper = new BabyCareTimelineEntity(
                    dateStr, timeStr, now, "DIAPER", "Diaper Change Recorded",
                    "Clean diaper logged via quick action", "#E6FAEA"
            );
            repo.insertBabyEvent(diaper, new MaaCareRepository.DataCallback<Long>() {
                @Override public void onSuccess(Long result) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Diaper change recorded!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        refreshCurrentFragment();
                    });
                }
                @Override public void onError(Throwable error) {}
            });
        });

        view.findViewById(R.id.btn_quick_sleep).setOnClickListener(v -> {
            BabyCareTimelineEntity sleep = new BabyCareTimelineEntity(
                    dateStr, timeStr, now, "SLEEP", "Baby Sleep / Nap Recorded",
                    "Peaceful sleep interval logged", "#FFF1E0"
            );
            repo.insertBabyEvent(sleep, new MaaCareRepository.DataCallback<Long>() {
                @Override public void onSuccess(Long result) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Sleep log recorded!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        refreshCurrentFragment();
                    });
                }
                @Override public void onError(Throwable error) {}
            });
        });

        view.findViewById(R.id.btn_quick_bath).setOnClickListener(v -> {
            BabyCareTimelineEntity bath = new BabyCareTimelineEntity(
                    dateStr, timeStr, now, "BATH", "Tub Bathing Recorded",
                    "Warm bath and hygiene logged", "#FDECEF"
            );
            repo.insertBabyEvent(bath, new MaaCareRepository.DataCallback<Long>() {
                @Override public void onSuccess(Long result) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Tub bath recorded!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        refreshCurrentFragment();
                    });
                }
                @Override public void onError(Throwable error) {}
            });
        });

        view.findViewById(R.id.btn_cancel_quick_log).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void refreshCurrentFragment() {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (current instanceof HomeTrackerFragment) {
            ((HomeTrackerFragment) current).refreshData();
        } else if (current instanceof CareAndVaccineFragment) {
            ((CareAndVaccineFragment) current).refreshData();
        }
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
