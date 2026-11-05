package com.cse2200.maacare.ui.fragments;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.cse2200.maacare.data.db.entity.HydrationLogEntity;
import com.cse2200.maacare.data.model.MaternalHealthDataResponse;
import com.cse2200.maacare.data.prefs.PreferenceManager;
import com.cse2200.maacare.data.repository.MaaCareRepository;
import com.cse2200.maacare.databinding.DialogEditHydrationBinding;
import com.cse2200.maacare.databinding.DialogEditProfileBinding;
import com.cse2200.maacare.databinding.FragmentHomeTrackerBinding;
import com.cse2200.maacare.service.MaternalHealthMonitorService;
import com.cse2200.maacare.ui.adapters.FetalMilestoneAdapter;
import com.cse2200.maacare.ui.auth.LoginActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Week-by-Week Pregnancy Tracker Fragment featuring:
 * - Deterministic, clock-driven gestational timeline engine (calculated from LMP)
 * - Custom circular arc gauge (PregnancyProgressArcView)
 * - Fully editable daily hydration controller & foreground service synchronization
 * - RecyclerView for weekly fetal development milestones
 */
public class HomeTrackerFragment extends Fragment {

    private FragmentHomeTrackerBinding binding;
    private PreferenceManager preferenceManager;
    private MaaCareRepository repository;
    private FetalMilestoneAdapter milestoneAdapter;

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final SimpleDateFormat DB_DATE_FMT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private final BroadcastReceiver waterUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateHydrationUI();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeTrackerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferenceManager = new PreferenceManager(requireContext());
        repository = new MaaCareRepository(requireContext());

        setupProfileAndArc();
        setupHydrationController();
        setupMilestonesRecyclerView();
        loadMilestoneData();
    }

    private void setupProfileAndArc() {
        updateProfileAndArcUI();

        binding.btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
    }

    private void updateProfileAndArcUI() {
        String name = preferenceManager.getMotherName();
        int week = preferenceManager.getCurrentWeek();
        int day = preferenceManager.getDayOfWeek();
        String blood = preferenceManager.getBloodGroup();
        int daysLeft = preferenceManager.getDaysRemainingToEdd();

        binding.tvMotherName.setText("Hello, " + name);
        binding.tvProfileSubtext.setText("Week " + week + ", Day " + day + " • Blood: " + blood + " • " + daysLeft + " Days to EDD");
        binding.tvCountdownEdd.setText(daysLeft + " Days to Estimated Delivery");

        // Update Custom View Arc
        binding.arcPregnancyProgress.setProgress(week, daysLeft);
    }

    private void setupHydrationController() {
        updateHydrationUI();
        updateServiceButtonState();

        binding.btnAdd250.setOnClickListener(v -> adjustWater(250));
        binding.btnAdd500.setOnClickListener(v -> adjustWater(500));
        binding.btnSub250.setOnClickListener(v -> adjustWater(-250));

        binding.btnEditHydration.setOnClickListener(v -> showEditHydrationDialog());

        binding.btnToggleService.setOnClickListener(v -> {
            boolean isRunning = preferenceManager.isMonitorActive();
            if (isRunning) {
                MaternalHealthMonitorService.stop(requireContext());
                preferenceManager.setMonitorActive(false);
                Toast.makeText(requireContext(), "Health Monitor Service Stopped", Toast.LENGTH_SHORT).show();
            } else {
                MaternalHealthMonitorService.start(requireContext());
                preferenceManager.setMonitorActive(true);
                Toast.makeText(requireContext(), "Foreground Monitor Service Active", Toast.LENGTH_SHORT).show();
            }
            updateServiceButtonState();
        });
    }

    private void adjustWater(int deltaMl) {
        preferenceManager.adjustTodayWaterIntakeMl(deltaMl);
        updateHydrationUI();

        // Persist to Room
        String today = DB_DATE_FMT.format(new Date());
        repository.insertHydrationLog(new HydrationLogEntity(today, deltaMl, System.currentTimeMillis()), new MaaCareRepository.DataCallback<Long>() {
            @Override
            public void onSuccess(Long id) {}

            @Override
            public void onError(Throwable error) {}
        });

        // If service is running, send intent to update notification
        if (preferenceManager.isMonitorActive()) {
            Intent intent = new Intent(requireContext(), MaternalHealthMonitorService.class);
            if (deltaMl == 250) intent.setAction(MaternalHealthMonitorService.ACTION_LOG_WATER_250);
            else if (deltaMl == 500) intent.setAction(MaternalHealthMonitorService.ACTION_LOG_WATER_500);
            else intent.setAction(MaternalHealthMonitorService.ACTION_DECREMENT_250);
            requireContext().startService(intent);
        }
    }

    private void updateHydrationUI() {
        int intake = preferenceManager.getTodayWaterIntakeMl();
        int goal = preferenceManager.getDailyWaterGoalMl();
        int percent = goal > 0 ? (int) (((double) intake / goal) * 100) : 0;

        binding.tvWaterCount.setText(intake + " mL / " + goal + " mL (" + percent + "%)");
        binding.pbWaterProgress.setProgress(Math.min(100, Math.max(0, percent)));
    }

    private void updateServiceButtonState() {
        boolean isRunning = preferenceManager.isMonitorActive();
        if (isRunning) {
            binding.btnToggleService.setText("Stop Foreground Monitor Service");
        } else {
            binding.btnToggleService.setText("Start Foreground Monitor Service");
        }
    }

    private void showEditHydrationDialog() {
        DialogEditHydrationBinding dBinding = DialogEditHydrationBinding.inflate(LayoutInflater.from(requireContext()));
        dBinding.etHydrationVolume.setText(String.valueOf(preferenceManager.getTodayWaterIntakeMl()));
        dBinding.etHydrationGoal.setText(String.valueOf(preferenceManager.getDailyWaterGoalMl()));

        new AlertDialog.Builder(requireContext())
                .setView(dBinding.getRoot())
                .setPositiveButton("Save", (dialog, which) -> {
                    String volStr = dBinding.etHydrationVolume.getText() != null ? dBinding.etHydrationVolume.getText().toString().trim() : "";
                    String goalStr = dBinding.etHydrationGoal.getText() != null ? dBinding.etHydrationGoal.getText().toString().trim() : "";

                    try {
                        if (!volStr.isEmpty()) {
                            int vol = Integer.parseInt(volStr);
                            preferenceManager.setTodayWaterIntakeMl(vol);
                        }
                        if (!goalStr.isEmpty()) {
                            int goal = Integer.parseInt(goalStr);
                            preferenceManager.setDailyWaterGoalMl(goal);
                        }
                        updateHydrationUI();
                        Toast.makeText(requireContext(), "Hydration updated", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException ignored) {}
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupMilestonesRecyclerView() {
        milestoneAdapter = new FetalMilestoneAdapter(milestone -> {
            Toast.makeText(requireContext(), "Week " + milestone.getWeek() + ": " + milestone.getBabySize(), Toast.LENGTH_SHORT).show();
        });
        binding.rvFetalMilestones.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFetalMilestones.setAdapter(milestoneAdapter);
    }

    private void loadMilestoneData() {
        repository.getMaternalHealthData(new MaaCareRepository.DataCallback<MaternalHealthDataResponse>() {
            @Override
            public void onSuccess(MaternalHealthDataResponse result) {
                if (getActivity() != null && result != null && result.getFetalMilestones() != null) {
                    getActivity().runOnUiThread(() -> milestoneAdapter.submitList(result.getFetalMilestones()));
                }
            }

            @Override
            public void onError(Throwable error) {}
        });
    }

    private void showEditProfileDialog() {
        DialogEditProfileBinding dialogBinding = DialogEditProfileBinding.inflate(LayoutInflater.from(requireContext()));
        dialogBinding.etProfileName.setText(preferenceManager.getMotherName());
        dialogBinding.etProfileBloodGroup.setText(preferenceManager.getBloodGroup());
        dialogBinding.etProfileEmergencyPhone.setText(preferenceManager.getEmergencyPhone());

        final long[] currentLmpHolder = new long[]{preferenceManager.getLmpDateMillis()};

        updateProfileLmpLabel(dialogBinding, currentLmpHolder[0]);

        dialogBinding.btnProfilePickLmp.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(currentLmpHolder[0]);

            DatePickerDialog picker = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar picked = Calendar.getInstance();
                        picked.set(year, month, dayOfMonth, 0, 0, 0);
                        currentLmpHolder[0] = picked.getTimeInMillis();
                        updateProfileLmpLabel(dialogBinding, currentLmpHolder[0]);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            picker.getDatePicker().setMaxDate(System.currentTimeMillis());
            picker.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save Changes", (d, which) -> {
                    String name = dialogBinding.etProfileName.getText() != null ? dialogBinding.etProfileName.getText().toString().trim() : "";
                    String blood = dialogBinding.etProfileBloodGroup.getText() != null ? dialogBinding.etProfileBloodGroup.getText().toString().trim() : "O+";
                    String phone = dialogBinding.etProfileEmergencyPhone.getText() != null ? dialogBinding.etProfileEmergencyPhone.getText().toString().trim() : "";

                    if (!name.isEmpty()) preferenceManager.setMotherName(name);
                    if (!blood.isEmpty()) preferenceManager.setBloodGroup(blood);
                    if (!phone.isEmpty()) preferenceManager.setEmergencyPhone(phone);
                    preferenceManager.setLmpDateMillis(currentLmpHolder[0]);

                    updateProfileAndArcUI();
                    Toast.makeText(requireContext(), "Profile & Timeline Updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialogBinding.btnProfileLogout.setOnClickListener(v -> {
            dialog.dismiss();
            preferenceManager.logout();
            Toast.makeText(requireContext(), "Signed Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });

        dialog.show();
    }

    private void updateProfileLmpLabel(DialogEditProfileBinding dBinding, long lmpMillis) {
        long elapsedDays = Math.max(0, (System.currentTimeMillis() - lmpMillis) / 86400000L);
        int currentWeek = (int) (elapsedDays / 7) + 1;
        int dayOfWeek = (int) (elapsedDays % 7);
        dBinding.tvProfileLmpDisplay.setText("LMP: " + DATE_FMT.format(new Date(lmpMillis))
                + " • Week " + currentWeek + ", Day " + dayOfWeek);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(MaternalHealthMonitorService.BROADCAST_WATER_UPDATED);
        requireContext().registerReceiver(waterUpdateReceiver, filter);
        updateProfileAndArcUI();
        updateHydrationUI();
        updateServiceButtonState();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            requireContext().unregisterReceiver(waterUpdateReceiver);
        } catch (IllegalArgumentException ignored) {}
    }

    public void refreshData() {
        if (binding != null && isAdded()) {
            updateProfileAndArcUI();
            updateHydrationUI();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
