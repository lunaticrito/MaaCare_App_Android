package com.cse2200.maacare.ui.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.cse2200.maacare.R;
import com.cse2200.maacare.data.db.entity.BabyCareTimelineEntity;
import com.cse2200.maacare.data.db.entity.BabyGrowthEntity;
import com.cse2200.maacare.data.db.entity.BabyMilestoneEntity;
import com.cse2200.maacare.data.db.entity.BabyVitalsEntity;
import com.cse2200.maacare.data.db.entity.VaccinationEntity;
import com.cse2200.maacare.data.prefs.PreferenceManager;
import com.cse2200.maacare.data.repository.MaaCareRepository;
import com.cse2200.maacare.databinding.FragmentCareAndVaccineBinding;
import com.cse2200.maacare.ui.adapters.BabyMilestoneAdapter;
import com.cse2200.maacare.ui.adapters.BabyTimelineAdapter;
import com.cse2200.maacare.ui.adapters.VaccinationAdapter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Segmented Baby Care Suite managing:
 * 1. Daily Timeline (Yesterday, Today, Tomorrow filters with Feeding, Diaper, Sleep, Bath logs)
 * 2. Growth Tracking (Anthropometrics, WHO Growth Reference Curve, Developmental Milestones)
 * 3. Health & Vitals Monitoring (Sickness notes, Temperature, Pulse, SpO2)
 * 4. WHO & EPI Child Immunization Timeline
 */
public class CareAndVaccineFragment extends Fragment {

    private FragmentCareAndVaccineBinding binding;
    private MaaCareRepository repository;
    private PreferenceManager preferenceManager;

    private BabyTimelineAdapter timelineAdapter;
    private BabyMilestoneAdapter milestoneAdapter;
    private VaccinationAdapter vaccineAdapter;

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final SimpleDateFormat DB_DATE_FMT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private String selectedTimelineDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCareAndVaccineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new MaaCareRepository(requireContext());
        preferenceManager = new PreferenceManager(requireContext());

        selectedTimelineDate = DB_DATE_FMT.format(new Date());

        setupBabyAgeBanner();
        setupSubmoduleToggles();
        setupTimelineSubmodule();
        setupGrowthSubmodule();
        setupHealthSubmodule();
        setupVaccinesSubmodule();

        loadAllData();
    }

    private void setupBabyAgeBanner() {
        updateBabyAgeUI();

        binding.btnEditBabyBirth.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(preferenceManager.getBabyBirthMillis());

            DatePickerDialog picker = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar picked = Calendar.getInstance();
                        picked.set(year, month, dayOfMonth, 0, 0, 0);
                        preferenceManager.setBabyBirthMillis(picked.getTimeInMillis());
                        updateBabyAgeUI();
                        loadVaccines();
                        Toast.makeText(requireContext(), "Birth date updated", Toast.LENGTH_SHORT).show();
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            picker.getDatePicker().setMaxDate(System.currentTimeMillis());
            picker.show();
        });
    }

    private void updateBabyAgeUI() {
        String ageFormatted = preferenceManager.getBabyAgeFormatted();
        binding.tvBabyAgeTitle.setText("Baby is " + ageFormatted);
        String birthDateStr = DATE_FMT.format(new Date(preferenceManager.getBabyBirthMillis()));
        binding.tvBabyBirthSubtext.setText("Born on " + birthDateStr + " • Chronological age milestone tracker.");
    }

    private void setupSubmoduleToggles() {
        binding.toggleBabySubmodules.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            binding.containerSubmoduleTimeline.setVisibility(View.GONE);
            binding.containerSubmoduleGrowth.setVisibility(View.GONE);
            binding.containerSubmoduleHealth.setVisibility(View.GONE);
            binding.containerSubmoduleVaccines.setVisibility(View.GONE);

            if (checkedId == R.id.btn_submodule_timeline) {
                binding.containerSubmoduleTimeline.setVisibility(View.VISIBLE);
                loadTimelineEvents();
            } else if (checkedId == R.id.btn_submodule_growth) {
                binding.containerSubmoduleGrowth.setVisibility(View.VISIBLE);
                loadGrowthAndMilestones();
            } else if (checkedId == R.id.btn_submodule_health) {
                binding.containerSubmoduleHealth.setVisibility(View.VISIBLE);
                loadHealthVitals();
            } else if (checkedId == R.id.btn_submodule_vaccines) {
                binding.containerSubmoduleVaccines.setVisibility(View.VISIBLE);
                loadVaccines();
            }
        });
    }

    private void setupTimelineSubmodule() {
        timelineAdapter = new BabyTimelineAdapter();
        binding.rvBabyTimeline.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBabyTimeline.setAdapter(timelineAdapter);

        binding.btnPillYesterday.setOnClickListener(v -> {
            highlightPill(binding.btnPillYesterday);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -1);
            selectedTimelineDate = DB_DATE_FMT.format(cal.getTime());
            loadTimelineEvents();
        });

        binding.btnPillToday.setOnClickListener(v -> {
            highlightPill(binding.btnPillToday);
            selectedTimelineDate = DB_DATE_FMT.format(new Date());
            loadTimelineEvents();
        });

        binding.btnPillTomorrow.setOnClickListener(v -> {
            highlightPill(binding.btnPillTomorrow);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 1);
            selectedTimelineDate = DB_DATE_FMT.format(cal.getTime());
            loadTimelineEvents();
        });
    }

    private void highlightPill(View selectedPill) {
        int primaryColor = ContextCompat.getColor(requireContext(), R.color.primary);
        int surfaceColor = ContextCompat.getColor(requireContext(), R.color.surface_card);
        int textColor = ContextCompat.getColor(requireContext(), R.color.text_primary);
        int whiteColor = ContextCompat.getColor(requireContext(), R.color.white);

        binding.btnPillYesterday.setBackgroundColor(surfaceColor);
        binding.btnPillYesterday.setTextColor(textColor);
        binding.btnPillToday.setBackgroundColor(surfaceColor);
        binding.btnPillToday.setTextColor(textColor);
        binding.btnPillTomorrow.setBackgroundColor(surfaceColor);
        binding.btnPillTomorrow.setTextColor(textColor);

        if (selectedPill instanceof com.google.android.material.button.MaterialButton) {
            ((com.google.android.material.button.MaterialButton) selectedPill).setBackgroundColor(primaryColor);
            ((com.google.android.material.button.MaterialButton) selectedPill).setTextColor(whiteColor);
        }
    }

    private void setupGrowthSubmodule() {
        milestoneAdapter = new BabyMilestoneAdapter((milestone, isAchieved, remarks) -> {
            repository.updateMilestone(milestone.getId(), isAchieved, remarks, new MaaCareRepository.DataCallback<Integer>() {
                @Override public void onSuccess(Integer result) {}
                @Override public void onError(Throwable error) {}
            });
        });
        binding.rvBabyMilestones.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBabyMilestones.setAdapter(milestoneAdapter);
    }

    private void setupHealthSubmodule() {
        binding.btnSaveVitals.setOnClickListener(v -> {
            boolean isSick = binding.rbSickYes.isChecked();
            String notes = binding.etSymptomsNotes.getText() != null ? binding.etSymptomsNotes.getText().toString().trim() : "";
            String tempDegree = binding.etTempValue.getText() != null ? binding.etTempValue.getText().toString().trim() : "98.6°F";
            int pulse = 120;
            try {
                pulse = Integer.parseInt(binding.etPulseValue.getText().toString().trim());
            } catch (Exception ignored) {}
            int spo2 = 98;
            try {
                spo2 = Integer.parseInt(binding.etSpo2Value.getText().toString().trim());
            } catch (Exception ignored) {}

            BabyVitalsEntity vitals = new BabyVitalsEntity(
                    isSick, notes, 98.6, 98.6, tempDegree, pulse, spo2, System.currentTimeMillis()
            );

            repository.insertVitals(vitals, new MaaCareRepository.DataCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Vital signs recorded successfully!", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
                @Override
                public void onError(Throwable error) {}
            });
        });
    }

    private void setupVaccinesSubmodule() {
        vaccineAdapter = new VaccinationAdapter(vaccine -> {
            repository.updateVaccineAdministered(vaccine.getId(), vaccine.isAdministered(), new MaaCareRepository.DataCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            String msg = vaccine.isAdministered() ? "Vaccine Marked Administered" : "Vaccine Marked Due";
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                            loadVaccines();
                        });
                    }
                }
                @Override
                public void onError(Throwable error) {}
            });
        });
        binding.rvVaccines.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvVaccines.setAdapter(vaccineAdapter);
    }

    private void loadAllData() {
        loadTimelineEvents();
        loadGrowthAndMilestones();
        loadHealthVitals();
        loadVaccines();
    }

    private void loadTimelineEvents() {
        repository.getAllBabyEvents(new MaaCareRepository.DataCallback<List<BabyCareTimelineEntity>>() {
            @Override
            public void onSuccess(List<BabyCareTimelineEntity> list) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> timelineAdapter.setEvents(list));
                }
            }
            @Override
            public void onError(Throwable error) {}
        });
    }

    private void loadGrowthAndMilestones() {
        repository.getAllGrowthRecords(new MaaCareRepository.DataCallback<List<BabyGrowthEntity>>() {
            @Override
            public void onSuccess(List<BabyGrowthEntity> records) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.chartBabyGrowth.setGrowthData(records);
                        if (records != null && !records.isEmpty()) {
                            BabyGrowthEntity birth = records.get(0);
                            BabyGrowthEntity latest = records.get(records.size() - 1);
                            binding.tvBirthMetrics.setText(String.format(Locale.getDefault(), "%.1f kg • %.1f cm", birth.getWeightKg(), birth.getHeightCm()));
                            binding.tvCurrentMetrics.setText(String.format(Locale.getDefault(), "%.1f kg • %.1f cm", latest.getWeightKg(), latest.getHeightCm()));
                        }
                    });
                }
            }
            @Override
            public void onError(Throwable error) {}
        });

        repository.getAllMilestones(new MaaCareRepository.DataCallback<List<BabyMilestoneEntity>>() {
            @Override
            public void onSuccess(List<BabyMilestoneEntity> list) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> milestoneAdapter.setMilestones(list));
                }
            }
            @Override
            public void onError(Throwable error) {}
        });
    }

    private void loadHealthVitals() {
        repository.getLatestVitals(new MaaCareRepository.DataCallback<BabyVitalsEntity>() {
            @Override
            public void onSuccess(BabyVitalsEntity vitals) {
                if (vitals != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (vitals.isSick()) {
                            binding.rbSickYes.setChecked(true);
                        } else {
                            binding.rbSickNo.setChecked(true);
                        }
                        if (vitals.getSymptomsNotes() != null) {
                            binding.etSymptomsNotes.setText(vitals.getSymptomsNotes());
                        }
                        if (vitals.getTempDegreeOrFreq() != null) {
                            binding.etTempValue.setText(vitals.getTempDegreeOrFreq());
                        }
                        binding.etPulseValue.setText(String.valueOf(vitals.getPulseFrequency()));
                        binding.etSpo2Value.setText(String.valueOf(vitals.getSpo2Frequency()));
                    });
                }
            }
            @Override
            public void onError(Throwable error) {}
        });
    }

    private void loadVaccines() {
        int babyDays = preferenceManager.getBabyAgeDays();
        vaccineAdapter.setCurrentBabyAgeDays(babyDays);

        repository.getAllVaccines(new MaaCareRepository.DataCallback<List<VaccinationEntity>>() {
            @Override
            public void onSuccess(List<VaccinationEntity> list) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> vaccineAdapter.setVaccines(list));
                }
            }
            @Override
            public void onError(Throwable error) {}
        });
    }

    public void refreshData() {
        if (binding != null && isAdded()) {
            updateBabyAgeUI();
            loadAllData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
