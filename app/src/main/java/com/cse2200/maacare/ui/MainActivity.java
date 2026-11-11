package com.cse2200.maacare.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.cse2200.maacare.R;
import com.cse2200.maacare.data.db.entity.BabyCareTimelineEntity;
import com.cse2200.maacare.data.prefs.PreferenceManager;
import com.cse2200.maacare.data.repository.MaaCareRepository;
import com.cse2200.maacare.databinding.ActivityMainBinding;
import com.cse2200.maacare.firebase.FirebaseManager;
import com.cse2200.maacare.receiver.NetworkStateChangeReceiver;
import com.cse2200.maacare.receiver.SystemBroadcastReceiver;
import com.cse2200.maacare.ui.auth.LoginActivity;
import com.cse2200.maacare.ui.fragments.CareAndVaccineFragment;
import com.cse2200.maacare.ui.fragments.EmergencyGuideFragment;
import com.cse2200.maacare.ui.fragments.HomeTrackerFragment;
import com.cse2200.maacare.ui.fragments.MedicineReminderFragment;
import com.cse2200.maacare.util.NotificationHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Main Activity hosting:
 * - 5-Slot Bottom Navigation & Center Quick-Log FAB
 * - Dynamic System Broadcast Receiver (Airplane Mode & Battery Low alerts)
 * - Network Connectivity Monitor
 * - Firebase Suite Realtime Cloud Synchronization
 */
public class MainActivity extends BaseActivity implements
        NetworkStateChangeReceiver.NetworkStateListener,
        SystemBroadcastReceiver.SystemBroadcastListener {

    private static final int PERMISSION_REQUEST_NOTIFICATIONS = 101;

    private ActivityMainBinding binding;
    private NetworkStateChangeReceiver networkReceiver;
    private SystemBroadcastReceiver systemReceiver;
    private FirebaseManager firebaseManager;

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

        firebaseManager = FirebaseManager.getInstance(this);

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

        // Initialize Dynamic BroadcastReceivers
        networkReceiver = new NetworkStateChangeReceiver(this);
        systemReceiver = new SystemBroadcastReceiver(this);
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
        SimpleDateFormat dbDateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar calYesterday = Calendar.getInstance();
        calYesterday.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayStr = dbDateFmt.format(calYesterday.getTime());
        String todayStr = dbDateFmt.format(new Date(now));
        Calendar calTomorrow = Calendar.getInstance();
        calTomorrow.add(Calendar.DAY_OF_YEAR, 1);
        String tomorrowStr = dbDateFmt.format(calTomorrow.getTime());

        String timeStr = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(now));
        String uid = preferenceManager.getFirebaseUid();

        ChipGroup dateChipGroup = view.findViewById(R.id.chip_group_quick_log_date);
        Fragment currentFrag = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFrag instanceof CareAndVaccineFragment) {
            String activeDate = ((CareAndVaccineFragment) currentFrag).getSelectedTimelineDate();
            if (yesterdayStr.equals(activeDate)) {
                dateChipGroup.check(R.id.chip_quick_yesterday);
            } else if (tomorrowStr.equals(activeDate)) {
                dateChipGroup.check(R.id.chip_quick_tomorrow);
            } else {
                dateChipGroup.check(R.id.chip_quick_today);
            }
        }

        view.findViewById(R.id.btn_quick_water).setOnClickListener(v -> {
            preferenceManager.adjustTodayWaterIntakeMl(250);
            int todayWater = preferenceManager.getTodayWaterIntakeMl();
            int goalWater = preferenceManager.getDailyWaterGoalMl();
            firebaseManager.syncRealtimeHydration(uid, todayWater, goalWater);

            Toast.makeText(this, "+250 mL logged! Realtime sync active.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            refreshCurrentFragment();
        });

        view.findViewById(R.id.btn_quick_feeding).setOnClickListener(v ->
                saveQuickTimelineEvent(repo, dialog, dateChipGroup, yesterdayStr, todayStr, tomorrowStr,
                        timeStr, now, uid, "FEEDING", "Baby Feeding Recorded",
                        "Logged via 1-tap quick action", "#E6F2FF", "Feeding recorded & backed up!"));

        view.findViewById(R.id.btn_quick_diaper).setOnClickListener(v ->
                saveQuickTimelineEvent(repo, dialog, dateChipGroup, yesterdayStr, todayStr, tomorrowStr,
                        timeStr, now, uid, "DIAPER", "Diaper Change Recorded",
                        "Clean diaper logged via quick action", "#E6FAEA", "Diaper change recorded & synced!"));

        view.findViewById(R.id.btn_quick_sleep).setOnClickListener(v ->
                saveQuickTimelineEvent(repo, dialog, dateChipGroup, yesterdayStr, todayStr, tomorrowStr,
                        timeStr, now, uid, "SLEEP", "Baby Sleep / Nap Recorded",
                        "Peaceful sleep interval logged", "#FFF1E0", "Sleep log recorded & synced!"));

        view.findViewById(R.id.btn_quick_bath).setOnClickListener(v ->
                saveQuickTimelineEvent(repo, dialog, dateChipGroup, yesterdayStr, todayStr, tomorrowStr,
                        timeStr, now, uid, "BATH", "Tub Bathing Recorded",
                        "Warm bath and hygiene logged", "#FDECEF", "Tub bath recorded & synced!"));

        view.findViewById(R.id.btn_cancel_quick_log).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void saveQuickTimelineEvent(MaaCareRepository repo, BottomSheetDialog dialog, ChipGroup dateChipGroup,
                                        String yesterdayStr, String todayStr, String tomorrowStr,
                                        String timeStr, long now, String uid,
                                        String type, String title, String details, String colorHex, String toastMsg) {
        String targetDate = todayStr;
        int checkedId = dateChipGroup != null ? dateChipGroup.getCheckedChipId() : R.id.chip_quick_today;
        if (checkedId == R.id.chip_quick_yesterday) targetDate = yesterdayStr;
        else if (checkedId == R.id.chip_quick_tomorrow) targetDate = tomorrowStr;

        BabyCareTimelineEntity entity = new BabyCareTimelineEntity(
                targetDate, timeStr, now, type, title, details, colorHex
        );
        repo.insertBabyEvent(entity, new MaaCareRepository.DataCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                entity.setId(result);
                firebaseManager.backupBabyEventToFirestore(uid, entity);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    refreshCurrentFragment();
                });
            }
            @Override
            public void onError(Throwable error) {}
        });
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
        // Register Dynamic BroadcastReceiver for Connectivity
        IntentFilter netFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, netFilter);

        // Register Dynamic System Broadcast Receiver for Airplane Mode & Low Battery
        IntentFilter sysFilter = new IntentFilter();
        sysFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        sysFilter.addAction(Intent.ACTION_BATTERY_LOW);
        registerReceiver(systemReceiver, sysFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(networkReceiver);
        } catch (IllegalArgumentException ignored) {}

        try {
            unregisterReceiver(systemReceiver);
        } catch (IllegalArgumentException ignored) {}
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        runOnUiThread(() -> {
            if (isConnected) {
                binding.tvOfflineBanner.setVisibility(View.GONE);
            } else {
                binding.tvOfflineBanner.setVisibility(View.VISIBLE);
                binding.tvOfflineBanner.setText(R.string.offline_banner_text);
                binding.tvOfflineBanner.setBackgroundColor(Color.parseColor("#D32F2F"));
            }
        });
    }

    @Override
    public void onAirplaneModeChanged(boolean isAirplaneModeOn) {
        runOnUiThread(() -> {
            if (isAirplaneModeOn) {
                binding.tvOfflineBanner.setVisibility(View.VISIBLE);
                binding.tvOfflineBanner.setText("✈️ Airplane Mode Active — MaaCare Offline Emergency Mode (Local Data Available)");
                binding.tvOfflineBanner.setBackgroundColor(Color.parseColor("#455A64"));

                Snackbar.make(binding.getRoot(),
                        "Airplane Mode: Switched to Offline Emergency Mode",
                        Snackbar.LENGTH_LONG)
                        .setAction("Details", v -> {
                            Toast.makeText(this, "Local emergency guides & offline alarms remain fully operational.", Toast.LENGTH_LONG).show();
                        }).show();
            } else {
                binding.tvOfflineBanner.setVisibility(View.GONE);
                Snackbar.make(binding.getRoot(),
                        "🌐 Online Cloud-Sync Mode Restored",
                        Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBatteryLow() {
        runOnUiThread(() -> {
            String phone = preferenceManager.getEmergencyPhone();
            String motherName = preferenceManager.getMotherName();
            int week = preferenceManager.getGestationalWeek();
            String blood = preferenceManager.getBloodGroup();

            new MaterialAlertDialogBuilder(this)
                    .setTitle("⚠️ Low Battery Alert")
                    .setMessage("Your device battery is running critically low. Would you like to dispatch an automated emergency SMS with your current pregnancy status (Week " + week + ", " + blood + ") to your emergency caregiver (" + phone + ")?")
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton("Send SMS Now", (dialog, which) -> {
                        String smsBody = "BATTERY LOW ALERT: Phone battery critically low for "
                                + motherName + " (Pregnancy Week: " + week
                                + ", Blood Group: " + blood + "). Please check on her!";
                        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                        smsIntent.setData(Uri.parse("smsto:" + phone));
                        smsIntent.putExtra("sms_body", smsBody);
                        try {
                            startActivity(smsIntent);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Unable to launch SMS composer.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Dismiss", null)
                    .show();
        });
    }
}
