package com.cse2200.maacare.ui.fragments;

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
import com.cse2200.maacare.R;
import com.cse2200.maacare.data.model.MaternalHealthDataResponse;
import com.cse2200.maacare.data.prefs.PreferenceManager;
import com.cse2200.maacare.data.repository.MaaCareRepository;
import com.cse2200.maacare.databinding.DialogEditProfileBinding;
import com.cse2200.maacare.databinding.FragmentHomeTrackerBinding;
import com.cse2200.maacare.service.MaternalHealthMonitorService;
import com.cse2200.maacare.ui.adapters.FetalMilestoneAdapter;

/**
 * Week-by-Week Pregnancy Tracker Fragment featuring:
 * - Dynamic maternal profile display & profile editor dialog
 * - Custom circular arc gauge (PregnancyProgressArcView)
 * - Maternal hydration monitor wired with Foreground Service
 * - RecyclerView for weekly fetal development milestones
 */
public class HomeTrackerFragment extends Fragment {

    private FragmentHomeTrackerBinding binding;
    private PreferenceManager preferenceManager;
    private MaaCareRepository repository;
    private FetalMilestoneAdapter milestoneAdapter;

    private final BroadcastReceiver waterUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateWaterIntakeUI();
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
        setupHydrationMonitor();
        setupMilestonesRecyclerView();
        loadMilestoneData();
    }

    private void setupProfileAndArc() {
        updateProfileUI();

        binding.btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
    }

    private void updateProfileUI() {
        String name = preferenceManager.getMotherName();
        int week = preferenceManager.getGestationalWeek();
        String blood = preferenceManager.getBloodGroup();
        int daysLeft = preferenceManager.getDaysRemainingToEdd();

        binding.tvMotherName.setText(getString(R.string.profile_greeting, name));
        binding.tvProfileSubtext.setText("Week " + week + " • Blood: " + blood + " • " + daysLeft + " Days to EDD");
        binding.tvCountdownEdd.setText(daysLeft + " Days to Estimated Delivery");

        // Update Custom View Arc
        binding.arcPregnancyProgress.setProgress(week, daysLeft);
    }

    private void setupHydrationMonitor() {
        updateWaterIntakeUI();
        updateServiceButtonState();

        binding.btnLogWater.setOnClickListener(v -> {
            preferenceManager.incrementWaterGlasses();
            updateWaterIntakeUI();
            Toast.makeText(requireContext(), "+250ml Water Logged", Toast.LENGTH_SHORT).show();
        });

        binding.btnToggleService.setOnClickListener(v -> {
            boolean isRunning = preferenceManager.isMonitorActive();
            if (isRunning) {
                MaternalHealthMonitorService.stop(requireContext());
                preferenceManager.setMonitorActive(false);
                Toast.makeText(requireContext(), "Health Monitor Service Stopped", Toast.LENGTH_SHORT).show();
            } else {
                MaternalHealthMonitorService.start(requireContext());
                preferenceManager.setMonitorActive(true);
                Toast.makeText(requireContext(), "Foreground Monitor Service Started", Toast.LENGTH_SHORT).show();
            }
            updateServiceButtonState();
        });
    }

    private void updateWaterIntakeUI() {
        int glasses = preferenceManager.getWaterGlasses();
        int ml = glasses * 250;
        binding.tvWaterCount.setText(glasses + " / 12 Glasses Logged Today (" + ml + " ml)");
    }

    private void updateServiceButtonState() {
        boolean isRunning = preferenceManager.isMonitorActive();
        if (isRunning) {
            binding.btnToggleService.setText(R.string.btn_stop_monitor);
        } else {
            binding.btnToggleService.setText(R.string.btn_start_monitor);
        }
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
            public void onError(Throwable error) {
                // Handled gracefully by repository fallback
            }
        });
    }

    private void showEditProfileDialog() {
        DialogEditProfileBinding dialogBinding = DialogEditProfileBinding.inflate(LayoutInflater.from(requireContext()));
        dialogBinding.etProfileName.setText(preferenceManager.getMotherName());
        dialogBinding.etProfileWeek.setText(String.valueOf(preferenceManager.getGestationalWeek()));
        dialogBinding.etProfileBloodGroup.setText(preferenceManager.getBloodGroup());
        dialogBinding.etProfileEmergencyPhone.setText(preferenceManager.getEmergencyPhone());

        new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = dialogBinding.etProfileName.getText() != null ? dialogBinding.etProfileName.getText().toString().trim() : "";
                    String weekStr = dialogBinding.etProfileWeek.getText() != null ? dialogBinding.etProfileWeek.getText().toString().trim() : "20";
                    String blood = dialogBinding.etProfileBloodGroup.getText() != null ? dialogBinding.etProfileBloodGroup.getText().toString().trim() : "O+";
                    String phone = dialogBinding.etProfileEmergencyPhone.getText() != null ? dialogBinding.etProfileEmergencyPhone.getText().toString().trim() : "";

                    if (!name.isEmpty()) preferenceManager.setMotherName(name);
                    try {
                        int week = Integer.parseInt(weekStr);
                        preferenceManager.setGestationalWeek(week);
                    } catch (NumberFormatException ignored) {}
                    if (!blood.isEmpty()) preferenceManager.setBloodGroup(blood);
                    if (!phone.isEmpty()) preferenceManager.setEmergencyPhone(phone);

                    updateProfileUI();
                    Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(MaternalHealthMonitorService.BROADCAST_WATER_UPDATED);
        requireContext().registerReceiver(waterUpdateReceiver, filter);
        updateProfileUI();
        updateWaterIntakeUI();
        updateServiceButtonState();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            requireContext().unregisterReceiver(waterUpdateReceiver);
        } catch (IllegalArgumentException ignored) {}
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
