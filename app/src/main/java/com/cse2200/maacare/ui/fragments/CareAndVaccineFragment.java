package com.cse2200.maacare.ui.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.cse2200.maacare.databinding.DialogEditGrowthBinding;
import com.cse2200.maacare.databinding.DialogEditTimelineEventBinding;
import com.cse2200.maacare.databinding.DialogEditVitalsBinding;
import com.cse2200.maacare.databinding.FragmentCareAndVaccineBinding;
import com.cse2200.maacare.firebase.FirebaseManager;
import com.cse2200.maacare.ui.adapters.BabyMilestoneAdapter;
import com.cse2200.maacare.ui.adapters.BabyTimelineAdapter;
import com.cse2200.maacare.ui.adapters.BabyVitalsAdapter;
import com.cse2200.maacare.ui.adapters.VaccinationAdapter;
import com.cse2200.maacare.util.LocaleHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Segmented Baby Care Suite managing full CRUD across:
 * 1. Daily Timeline (Date-filtered Yesterday / Today / Tomorrow with Edit & Delete + Undo)
 * 2. Growth Tracking (Interactive Weight/Height Measurements, live WHO Curve, Milestones)
 * 3. Health & Vitals Monitoring (Persisted Vitals + Edit/Delete History + Realtime DB Sync)
 * 4. WHO & EPI Child Immunization Timeline
 */
public class CareAndVaccineFragment extends Fragment implements
        BabyTimelineAdapter.OnTimelineActionListener,
        BabyVitalsAdapter.OnVitalsActionListener {

    private static final String TAG = "CareAndVaccineFragment";

    private FragmentCareAndVaccineBinding binding;
    private MaaCareRepository repository;
    private PreferenceManager preferenceManager;

    private BabyTimelineAdapter timelineAdapter;
    private BabyMilestoneAdapter milestoneAdapter;
    private BabyVitalsAdapter vitalsAdapter;
    private VaccinationAdapter vaccineAdapter;

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final SimpleDateFormat DB_DATE_FMT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    private String selectedTimelineDate;
    private final List<BabyGrowthEntity> cachedGrowthRecords = new ArrayList<>();

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

    public String getSelectedTimelineDate() {
        return selectedTimelineDate != null ? selectedTimelineDate : DB_DATE_FMT.format(new Date());
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
        String birthDateStr = DATE_FMT.format(new Date(preferenceManager.getBabyBirthMillis()));
        if (LocaleHelper.isBangla(requireContext())) {
            int days = preferenceManager.getBabyAgeDays();
            int weeks = days / 7;
            int months = days / 30;
            String ageBn = months >= 1
                    ? LocaleHelper.toBanglaDigits(weeks + " সপ্তাহ (" + months + " মাস)")
                    : LocaleHelper.toBanglaDigits(days + " দিন");
            binding.tvBabyAgeTitle.setText("শিশুর বয়স " + ageBn);
            binding.tvBabyBirthSubtext.setText("জন্ম তারিখ: " + LocaleHelper.toBanglaDigits(birthDateStr) + " • বয়স অনুযায়ী টিকা ও বিকাশ পর্যবেক্ষণ।");
        } else {
            binding.tvBabyAgeTitle.setText("Baby is " + ageFormatted);
            binding.tvBabyBirthSubtext.setText("Born on " + birthDateStr + " • Chronological age milestone tracker.");
        }
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

    // =========================================================================
    // Sub-module 1: Date-Filtered & Editable Baby Daily Timeline
    // =========================================================================

    private void setupTimelineSubmodule() {
        timelineAdapter = new BabyTimelineAdapter(this);
        binding.rvBabyTimeline.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBabyTimeline.setAdapter(timelineAdapter);

        highlightPill(binding.btnPillToday);

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

        binding.btnAddTimelineEvent.setOnClickListener(v -> showTimelineEventDialog(null));
    }

    private void highlightPill(MaterialButton selectedPill) {
        int activeBg = ContextCompat.getColor(requireContext(), R.color.chip_checked_bg);
        int activeText = ContextCompat.getColor(requireContext(), R.color.chip_checked_text);
        int inactiveBg = ContextCompat.getColor(requireContext(), R.color.surface_card);
        int inactiveText = ContextCompat.getColor(requireContext(), R.color.text_primary);

        MaterialButton[] pills = {binding.btnPillYesterday, binding.btnPillToday, binding.btnPillTomorrow};
        for (MaterialButton pill : pills) {
            boolean isSel = (pill == selectedPill);
            pill.setBackgroundColor(isSel ? activeBg : inactiveBg);
            pill.setTextColor(isSel ? activeText : inactiveText);
        }
    }

    @Override
    public void onEditTimelineEvent(BabyCareTimelineEntity event) {
        showTimelineEventDialog(event);
    }

    @Override
    public void onDeleteTimelineEvent(BabyCareTimelineEntity event) {
        repository.deleteBabyEvent(event, new MaaCareRepository.DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                String uid = preferenceManager.getFirebaseUid();
                FirebaseManager.getInstance(requireContext()).deleteBabyEventFromFirestore(uid, event.getId());

                if (getActivity() != null && binding != null) {
                    getActivity().runOnUiThread(() -> {
                        loadTimelineEvents();
                        Snackbar.make(binding.getRoot(), "Removed: " + event.getTitle(), Snackbar.LENGTH_LONG)
                                .setAction("UNDO", v -> {
                                    repository.insertBabyEvent(event, new MaaCareRepository.DataCallback<Long>() {
                                        @Override
                                        public void onSuccess(Long restoredId) {
                                            event.setId(restoredId);
                                            FirebaseManager.getInstance(requireContext()).backupBabyEventToFirestore(uid, event);
                                            if (getActivity() != null) {
                                                getActivity().runOnUiThread(() -> loadTimelineEvents());
                                            }
                                        }
                                        @Override public void onError(Throwable error) {}
                                    });
                                })
                                .show();
                    });
                }
            }
            @Override public void onError(Throwable error) {}
        });
    }

    private void showTimelineEventDialog(@Nullable BabyCareTimelineEntity existingEvent) {
        DialogEditTimelineEventBinding dialogBinding = DialogEditTimelineEventBinding.inflate(getLayoutInflater());
        boolean isEdit = existingEvent != null;

        dialogBinding.tvDialogTimelineHeader.setText(isEdit ? "Edit Care Activity" : "Add Care Activity (" + selectedTimelineDate + ")");

        final String[] chosenTime = new String[]{
                isEdit && existingEvent.getTimeFormatted() != null
                        ? existingEvent.getTimeFormatted()
                        : TIME_FMT.format(new Date())
        };
        dialogBinding.tvTimelineTimeSelected.setText("Time: " + chosenTime[0]);

        if (isEdit) {
            dialogBinding.etTimelineTitle.setText(existingEvent.getTitle());
            dialogBinding.etTimelineDetails.setText(existingEvent.getDetails());
            String type = existingEvent.getActivityType();
            if ("DIAPER".equalsIgnoreCase(type)) dialogBinding.chipCatDiaper.setChecked(true);
            else if ("SLEEP".equalsIgnoreCase(type)) dialogBinding.chipCatSleep.setChecked(true);
            else if ("BATH".equalsIgnoreCase(type)) dialogBinding.chipCatBath.setChecked(true);
            else dialogBinding.chipCatFeeding.setChecked(true);
        } else {
            dialogBinding.chipCatFeeding.setChecked(true);
        }

        dialogBinding.btnPickTimelineTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            TimePickerDialog picker = new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
                Calendar picked = Calendar.getInstance();
                picked.set(Calendar.HOUR_OF_DAY, hourOfDay);
                picked.set(Calendar.MINUTE, minute);
                chosenTime[0] = TIME_FMT.format(picked.getTime());
                dialogBinding.tvTimelineTimeSelected.setText("Time: " + chosenTime[0]);
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false);
            picker.show();
        });

        new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton(isEdit ? "Update" : "Save", (d, which) -> {
                    int checkedChipId = dialogBinding.chipGroupTimelineCategory.getCheckedChipId();
                    String category = "FEEDING";
                    String catColor = "#E6F2FF";
                    if (checkedChipId == R.id.chip_cat_diaper) {
                        category = "DIAPER";
                        catColor = "#E6FAEA";
                    } else if (checkedChipId == R.id.chip_cat_sleep) {
                        category = "SLEEP";
                        catColor = "#FFF1E0";
                    } else if (checkedChipId == R.id.chip_cat_bath) {
                        category = "BATH";
                        catColor = "#FDECEF";
                    }

                    String title = dialogBinding.etTimelineTitle.getText() != null
                            ? dialogBinding.etTimelineTitle.getText().toString().trim() : "";
                    String details = dialogBinding.etTimelineDetails.getText() != null
                            ? dialogBinding.etTimelineDetails.getText().toString().trim() : "";

                    if (title.isEmpty()) {
                        title = category.substring(0, 1) + category.substring(1).toLowerCase(Locale.getDefault()) + " Log";
                    }
                    if (details.isEmpty()) {
                        details = "Logged for " + selectedTimelineDate;
                    }

                    String uid = preferenceManager.getFirebaseUid();
                    if (isEdit) {
                        existingEvent.setActivityType(category);
                        existingEvent.setCategoryColor(catColor);
                        existingEvent.setTitle(title);
                        existingEvent.setDetails(details);
                        existingEvent.setTimeFormatted(chosenTime[0]);

                        repository.updateBabyEvent(existingEvent, new MaaCareRepository.DataCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer result) {
                                FirebaseManager.getInstance(requireContext()).backupBabyEventToFirestore(uid, existingEvent);
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(requireContext(), "Care activity updated", Toast.LENGTH_SHORT).show();
                                        loadTimelineEvents();
                                    });
                                }
                            }
                            @Override public void onError(Throwable error) {}
                        });
                    } else {
                        BabyCareTimelineEntity newEvent = new BabyCareTimelineEntity(
                                selectedTimelineDate, chosenTime[0], System.currentTimeMillis(),
                                category, title, details, catColor
                        );
                        repository.insertBabyEvent(newEvent, new MaaCareRepository.DataCallback<Long>() {
                            @Override
                            public void onSuccess(Long id) {
                                newEvent.setId(id);
                                FirebaseManager.getInstance(requireContext()).backupBabyEventToFirestore(uid, newEvent);
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(requireContext(), "Care activity saved", Toast.LENGTH_SHORT).show();
                                        loadTimelineEvents();
                                    });
                                }
                            }
                            @Override public void onError(Throwable error) {}
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // =========================================================================
    // Sub-module 2: Interactive Growth Measurements & WHO Chart
    // =========================================================================

    private void setupGrowthSubmodule() {
        milestoneAdapter = new BabyMilestoneAdapter((milestone, isAchieved, remarks) -> {
            repository.updateMilestone(milestone.getId(), isAchieved, remarks, new MaaCareRepository.DataCallback<Integer>() {
                @Override public void onSuccess(Integer result) {}
                @Override public void onError(Throwable error) {}
            });
        });
        binding.rvBabyMilestones.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBabyMilestones.setAdapter(milestoneAdapter);

        binding.cardBirthGrowth.setOnClickListener(v -> showGrowthMeasurementDialog(0));
        binding.cardCurrentGrowth.setOnClickListener(v -> {
            int latestMonth = cachedGrowthRecords.isEmpty()
                    ? 2
                    : cachedGrowthRecords.get(cachedGrowthRecords.size() - 1).getAgeMonths();
            showGrowthMeasurementDialog(latestMonth);
        });
        binding.btnAddEditGrowth.setOnClickListener(v -> {
            int latestMonth = cachedGrowthRecords.isEmpty()
                    ? 2
                    : cachedGrowthRecords.get(cachedGrowthRecords.size() - 1).getAgeMonths();
            showGrowthMeasurementDialog(latestMonth);
        });
    }

    private void showGrowthMeasurementDialog(int preselectMonth) {
        DialogEditGrowthBinding dialogBinding = DialogEditGrowthBinding.inflate(getLayoutInflater());

        final int[] selectedMonth = new int[]{preselectMonth};
        checkChipForMonth(dialogBinding, preselectMonth);
        populateGrowthInputsForMonth(dialogBinding, preselectMonth);

        dialogBinding.chipGroupGrowthMonth.setOnCheckedChangeListener((group, checkedId) -> {
            int month = getMonthFromChipId(checkedId);
            selectedMonth[0] = month;
            populateGrowthInputsForMonth(dialogBinding, month);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save Measurement", (d, which) -> {
                    String weightStr = dialogBinding.etGrowthWeight.getText() != null
                            ? dialogBinding.etGrowthWeight.getText().toString().trim() : "";
                    String heightStr = dialogBinding.etGrowthHeight.getText() != null
                            ? dialogBinding.etGrowthHeight.getText().toString().trim() : "";

                    if (weightStr.isEmpty() || heightStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter both weight and height", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double weightKg;
                    double heightCm;
                    try {
                        weightKg = Double.parseDouble(weightStr);
                        heightCm = Double.parseDouble(heightStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Enter valid numeric values", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (weightKg < 0.5 || weightKg > 35.0 || heightCm < 25.0 || heightCm > 135.0) {
                        Toast.makeText(requireContext(), "Weight (0.5–35 kg) or Height (25–135 cm) out of range", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int targetMonth = selectedMonth[0];
                    repository.upsertGrowthRecord(targetMonth, heightCm, weightKg, new MaaCareRepository.DataCallback<BabyGrowthEntity>() {
                        @Override
                        public void onSuccess(BabyGrowthEntity savedRecord) {
                            String uid = preferenceManager.getFirebaseUid();
                            FirebaseManager.getInstance(requireContext()).syncGrowthRecordToFirestore(uid, savedRecord);

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    loadGrowthAndMilestones();
                                    Toast.makeText(requireContext(), "Growth chart & metrics updated!", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                        @Override public void onError(Throwable error) {}
                    });
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Delete Month", (d, which) -> {
                    int targetMonth = selectedMonth[0];
                    if (targetMonth == 0) {
                        Toast.makeText(requireContext(), "Birth measurement cannot be deleted; edit it instead.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    BabyGrowthEntity target = findGrowthByMonth(targetMonth);
                    if (target == null) {
                        Toast.makeText(requireContext(), "No saved measurement for " + targetMonth + "m to delete.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    repository.deleteGrowthRecord(target, new MaaCareRepository.DataCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer result) {
                            String uid = preferenceManager.getFirebaseUid();
                            FirebaseManager.getInstance(requireContext()).deleteGrowthRecordFromFirestore(uid, targetMonth);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    loadGrowthAndMilestones();
                                    Toast.makeText(requireContext(), "Deleted " + targetMonth + "m measurement", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                        @Override public void onError(Throwable error) {}
                    });
                });

        builder.show();
    }

    private void checkChipForMonth(DialogEditGrowthBinding db, int month) {
        if (month == 0) db.chipMonth0.setChecked(true);
        else if (month == 1) db.chipMonth1.setChecked(true);
        else if (month == 2) db.chipMonth2.setChecked(true);
        else if (month == 3) db.chipMonth3.setChecked(true);
        else if (month == 4) db.chipMonth4.setChecked(true);
        else if (month == 5) db.chipMonth5.setChecked(true);
        else if (month == 6) db.chipMonth6.setChecked(true);
        else if (month == 9) db.chipMonth9.setChecked(true);
        else if (month == 12) db.chipMonth12.setChecked(true);
        else db.chipMonth2.setChecked(true);
    }

    private int getMonthFromChipId(int chipId) {
        if (chipId == R.id.chip_month_0) return 0;
        if (chipId == R.id.chip_month_1) return 1;
        if (chipId == R.id.chip_month_2) return 2;
        if (chipId == R.id.chip_month_3) return 3;
        if (chipId == R.id.chip_month_4) return 4;
        if (chipId == R.id.chip_month_5) return 5;
        if (chipId == R.id.chip_month_6) return 6;
        if (chipId == R.id.chip_month_9) return 9;
        if (chipId == R.id.chip_month_12) return 12;
        return 0;
    }

    private void populateGrowthInputsForMonth(DialogEditGrowthBinding db, int month) {
        BabyGrowthEntity existing = findGrowthByMonth(month);
        if (existing != null) {
            db.etGrowthWeight.setText(String.format(Locale.US, "%.1f", existing.getWeightKg()));
            db.etGrowthHeight.setText(String.format(Locale.US, "%.1f", existing.getHeightCm()));
            db.tvGrowthExistingHint.setVisibility(View.VISIBLE);
            db.tvGrowthExistingHint.setText("Editing saved measurement for " + (month == 0 ? "Birth (0m)" : month + "m"));
        } else {
            db.etGrowthWeight.setText("");
            db.etGrowthHeight.setText("");
            db.tvGrowthExistingHint.setVisibility(View.VISIBLE);
            db.tvGrowthExistingHint.setText("Adding new measurement for " + month + "m");
        }
    }

    @Nullable
    private BabyGrowthEntity findGrowthByMonth(int month) {
        for (BabyGrowthEntity g : cachedGrowthRecords) {
            if (g.getAgeMonths() == month) return g;
        }
        return null;
    }

    // =========================================================================
    // Sub-module 3: Health & Vitals Persistence + History List
    // =========================================================================

    private void setupHealthSubmodule() {
        vitalsAdapter = new BabyVitalsAdapter(this);
        binding.rvVitalsHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvVitalsHistory.setAdapter(vitalsAdapter);

        // Attach Realtime Database vitals listener
        String uid = preferenceManager.getFirebaseUid();
        FirebaseManager.getInstance(requireContext()).listenToRealtimeVitals(uid, vitalsData -> {
            Log.d(TAG, "Realtime vitals update received: " + vitalsData.size() + " fields");
        });

        binding.btnSaveVitals.setOnClickListener(v -> {
            boolean isSick = binding.rbSickYes.isChecked();
            String notes = binding.etSymptomsNotes.getText() != null
                    ? binding.etSymptomsNotes.getText().toString().trim() : "";
            String tempDegree = binding.etTempValue.getText() != null
                    ? binding.etTempValue.getText().toString().trim() : "";
            String pulseStr = binding.etPulseValue.getText() != null
                    ? binding.etPulseValue.getText().toString().trim() : "";
            String spo2Str = binding.etSpo2Value.getText() != null
                    ? binding.etSpo2Value.getText().toString().trim() : "";

            if (tempDegree.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter temperature", Toast.LENGTH_SHORT).show();
                return;
            }

            int pulse;
            int spo2;
            try {
                pulse = Integer.parseInt(pulseStr);
                spo2 = Integer.parseInt(spo2Str);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Please enter valid numeric Pulse & SpO2", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pulse < 30 || pulse > 250 || spo2 < 50 || spo2 > 100) {
                Toast.makeText(requireContext(), "Pulse (30–250 bpm) or SpO2 (50–100%) out of range", Toast.LENGTH_SHORT).show();
                return;
            }

            double tempNumeric = 98.6;
            try {
                String cleaned = tempDegree.replaceAll("[^0-9.]", " ").trim().split("\\s+")[0];
                tempNumeric = Double.parseDouble(cleaned);
            } catch (Exception ignored) {}

            if (notes.isEmpty()) {
                notes = isSick ? "Symptoms observed; monitoring closely." : "Baby is energetic, active and feeding well.";
            }

            BabyVitalsEntity vitals = new BabyVitalsEntity(
                    isSick, notes, tempNumeric, tempNumeric, tempDegree, pulse, spo2, System.currentTimeMillis()
            );

            double finalTempNumeric = tempNumeric;
            repository.insertVitals(vitals, new MaaCareRepository.DataCallback<Long>() {
                @Override
                public void onSuccess(Long id) {
                    vitals.setId(id);
                    String currentUid = preferenceManager.getFirebaseUid();
                    FirebaseManager.getInstance(requireContext()).syncRealtimeBabyVitals(
                            currentUid, isSick, finalTempNumeric, pulse, spo2, vitals.getSymptomsNotes()
                    );
                    FirebaseManager.getInstance(requireContext()).backupVitalsToFirestore(currentUid, vitals);

                    if (getActivity() != null && binding != null) {
                        getActivity().runOnUiThread(() -> {
                            Snackbar.make(binding.getRoot(), "Health & Vitals saved successfully!", Snackbar.LENGTH_SHORT).show();
                            loadHealthVitals();
                        });
                    }
                }
                @Override public void onError(Throwable error) {}
            });
        });
    }

    @Override
    public void onEditVitals(BabyVitalsEntity vitals) {
        DialogEditVitalsBinding db = DialogEditVitalsBinding.inflate(getLayoutInflater());
        if (vitals.isSick()) db.rbEditSickYes.setChecked(true);
        else db.rbEditSickNo.setChecked(true);

        db.etEditVitalsNotes.setText(vitals.getSymptomsNotes());
        db.etEditVitalsTemp.setText(vitals.getTempDegreeOrFreq());
        db.etEditVitalsPulse.setText(String.valueOf(vitals.getPulseFrequency()));
        db.etEditVitalsSpo2.setText(String.valueOf(vitals.getSpo2Frequency()));

        new AlertDialog.Builder(requireContext())
                .setView(db.getRoot())
                .setPositiveButton("Update", (d, which) -> {
                    boolean isSick = db.rbEditSickYes.isChecked();
                    String notes = db.etEditVitalsNotes.getText() != null ? db.etEditVitalsNotes.getText().toString().trim() : "";
                    String tempStr = db.etEditVitalsTemp.getText() != null ? db.etEditVitalsTemp.getText().toString().trim() : "98.6°F";
                    int pulse = vitals.getPulseFrequency();
                    int spo2 = vitals.getSpo2Frequency();
                    try {
                        pulse = Integer.parseInt(db.etEditVitalsPulse.getText().toString().trim());
                        spo2 = Integer.parseInt(db.etEditVitalsSpo2.getText().toString().trim());
                    } catch (Exception ignored) {}

                    vitals.setSick(isSick);
                    vitals.setSymptomsNotes(notes);
                    vitals.setTempDegreeOrFreq(tempStr);
                    vitals.setPulseFrequency(pulse);
                    vitals.setSpo2Frequency(spo2);

                    double tempNumeric = 98.6;
                    try {
                        String cleaned = tempStr.replaceAll("[^0-9.]", " ").trim().split("\\s+")[0];
                        tempNumeric = Double.parseDouble(cleaned);
                    } catch (Exception ignored) {}

                    double finalTemp = tempNumeric;
                    int finalPulse = pulse;
                    int finalSpo2 = spo2;

                    repository.updateVitals(vitals, new MaaCareRepository.DataCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer result) {
                            String uid = preferenceManager.getFirebaseUid();
                            FirebaseManager.getInstance(requireContext()).syncRealtimeBabyVitals(uid, isSick, finalTemp, finalPulse, finalSpo2, notes);
                            FirebaseManager.getInstance(requireContext()).backupVitalsToFirestore(uid, vitals);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "Vitals reading updated", Toast.LENGTH_SHORT).show();
                                    loadHealthVitals();
                                });
                            }
                        }
                        @Override public void onError(Throwable error) {}
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDeleteVitals(BabyVitalsEntity vitals) {
        repository.deleteVitals(vitals, new MaaCareRepository.DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                String uid = preferenceManager.getFirebaseUid();
                FirebaseManager.getInstance(requireContext()).deleteVitalsFromFirestore(uid, vitals.getId());
                if (getActivity() != null && binding != null) {
                    getActivity().runOnUiThread(() -> {
                        loadHealthVitals();
                        Snackbar.make(binding.getRoot(), "Vitals entry deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", v -> {
                                    repository.insertVitals(vitals, new MaaCareRepository.DataCallback<Long>() {
                                        @Override
                                        public void onSuccess(Long restoredId) {
                                            vitals.setId(restoredId);
                                            FirebaseManager.getInstance(requireContext()).backupVitalsToFirestore(uid, vitals);
                                            if (getActivity() != null) {
                                                getActivity().runOnUiThread(() -> loadHealthVitals());
                                            }
                                        }
                                        @Override public void onError(Throwable error) {}
                                    });
                                })
                                .show();
                    });
                }
            }
            @Override public void onError(Throwable error) {}
        });
    }

    // =========================================================================
    // Sub-module 4: Vaccines & Data Loaders
    // =========================================================================

    private void setupVaccinesSubmodule() {
        vaccineAdapter = new VaccinationAdapter((vaccine, isDone) -> {
            vaccine.setDone(isDone);
            repository.updateVaccineAdministered(vaccine.getId(), isDone, new MaaCareRepository.DataCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    String uid = preferenceManager.getFirebaseUid();
                    FirebaseManager.getInstance(requireContext()).backupVaccineToFirestore(uid, vaccine);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            String msg = isDone ? "Vaccine Marked Administered" : "Vaccine Marked Due";
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
        String dateFilter = getSelectedTimelineDate();
        repository.getBabyEventsByDate(dateFilter, new MaaCareRepository.DataCallback<List<BabyCareTimelineEntity>>() {
            @Override
            public void onSuccess(List<BabyCareTimelineEntity> list) {
                if (getActivity() != null && binding != null) {
                    getActivity().runOnUiThread(() -> {
                        timelineAdapter.setEvents(list);
                        boolean empty = (list == null || list.isEmpty());
                        binding.tvTimelineEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                    });
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
                if (getActivity() != null && binding != null) {
                    getActivity().runOnUiThread(() -> {
                        cachedGrowthRecords.clear();
                        if (records != null) {
                            cachedGrowthRecords.addAll(records);
                        }
                        binding.chartBabyGrowth.setGrowthData(cachedGrowthRecords);
                        binding.chartBabyGrowth.invalidate();

                        if (!cachedGrowthRecords.isEmpty()) {
                            boolean bn = LocaleHelper.isBangla(requireContext());
                            BabyGrowthEntity birth = cachedGrowthRecords.get(0);
                            BabyGrowthEntity latest = cachedGrowthRecords.get(cachedGrowthRecords.size() - 1);

                            if (bn) {
                                binding.tvBirthMetrics.setText(LocaleHelper.toBanglaDigits(
                                        String.format(Locale.US, "%.1f কেজি • %.1f সে.মি.", birth.getWeightKg(), birth.getHeightCm())));
                                String ageLabelBn = latest.getAgeMonths() == 0
                                        ? "বর্তমান (জন্মের সময়)"
                                        : "বর্তমান (" + LocaleHelper.toBanglaDigits(String.valueOf(latest.getAgeMonths())) + " মাস)";
                                binding.tvCurrentGrowthLabel.setText(ageLabelBn);
                                binding.tvCurrentMetrics.setText(LocaleHelper.toBanglaDigits(
                                        String.format(Locale.US, "%.1f কেজি • %.1f সে.মি.", latest.getWeightKg(), latest.getHeightCm())));

                                StringBuilder summary = new StringBuilder("সংরক্ষিত: ");
                                for (int i = 0; i < cachedGrowthRecords.size(); i++) {
                                    BabyGrowthEntity g = cachedGrowthRecords.get(i);
                                    if (i > 0) summary.append(" • ");
                                    summary.append(g.getAgeMonths() == 0 ? "জন্ম" : LocaleHelper.toBanglaDigits(String.valueOf(g.getAgeMonths())) + "ম")
                                            .append(" ")
                                            .append(LocaleHelper.toBanglaDigits(String.format(Locale.US, "%.1f কেজি", g.getWeightKg())));
                                }
                                binding.tvGrowthPointsSummary.setText(summary.toString());
                            } else {
                                binding.tvBirthMetrics.setText(String.format(Locale.getDefault(), "%.1f kg • %.1f cm", birth.getWeightKg(), birth.getHeightCm()));
                                String ageLabel = latest.getAgeMonths() == 0
                                        ? "Current (At Birth)"
                                        : "Current (" + latest.getAgeMonths() + (latest.getAgeMonths() == 1 ? " Month)" : " Months)");
                                binding.tvCurrentGrowthLabel.setText(ageLabel);
                                binding.tvCurrentMetrics.setText(String.format(Locale.getDefault(), "%.1f kg • %.1f cm", latest.getWeightKg(), latest.getHeightCm()));

                                StringBuilder summary = new StringBuilder("Logged: ");
                                for (int i = 0; i < cachedGrowthRecords.size(); i++) {
                                    BabyGrowthEntity g = cachedGrowthRecords.get(i);
                                    if (i > 0) summary.append(" • ");
                                    summary.append(g.getAgeMonths() == 0 ? "Birth" : g.getAgeMonths() + "m")
                                            .append(" ")
                                            .append(String.format(Locale.getDefault(), "%.1fkg", g.getWeightKg()));
                                }
                                binding.tvGrowthPointsSummary.setText(summary.toString());
                            }
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
                if (getActivity() != null && binding != null) {
                    getActivity().runOnUiThread(() -> milestoneAdapter.setMilestones(list));
                }
            }
            @Override
            public void onError(Throwable error) {}
        });
    }

    private void loadHealthVitals() {
        repository.getAllVitals(new MaaCareRepository.DataCallback<List<BabyVitalsEntity>>() {
            @Override
            public void onSuccess(List<BabyVitalsEntity> list) {
                if (getActivity() != null && binding != null) {
                    getActivity().runOnUiThread(() -> {
                        vitalsAdapter.submitList(list);
                        boolean empty = (list == null || list.isEmpty());
                        binding.tvVitalsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);

                        if (list != null && !list.isEmpty()) {
                            BabyVitalsEntity latest = list.get(0);
                            if (latest.isSick()) binding.rbSickYes.setChecked(true);
                            else binding.rbSickNo.setChecked(true);

                            if (latest.getSymptomsNotes() != null) {
                                binding.etSymptomsNotes.setText(LocaleHelper.translateDynamicText(requireContext(), latest.getSymptomsNotes()));
                            }
                            if (latest.getTempDegreeOrFreq() != null) {
                                String tempText = LocaleHelper.isBangla(requireContext())
                                        ? LocaleHelper.toBanglaDigits(latest.getTempDegreeOrFreq().replace("Normal", "স্বাভাবিক"))
                                        : latest.getTempDegreeOrFreq();
                                binding.etTempValue.setText(tempText);
                            }
                            binding.etPulseValue.setText(String.valueOf(latest.getPulseFrequency()));
                            binding.etSpo2Value.setText(String.valueOf(latest.getSpo2Frequency()));
                        }
                    });
                }
            }
            @Override
            public void onError(Throwable error) {}
        });
    }

    private void loadVaccines() {
        int babyDays = preferenceManager.getBabyAgeDays();
        vaccineAdapter.setBabyAgeDays(babyDays);

        repository.getAllVaccines(new MaaCareRepository.DataCallback<List<VaccinationEntity>>() {
            @Override
            public void onSuccess(List<VaccinationEntity> list) {
                if (getActivity() != null && binding != null) {
                    getActivity().runOnUiThread(() -> vaccineAdapter.submitList(list));
                }
            }
            @Override
            public void onError(Throwable error) {}
        });
    }

    public void refreshData() {
        if (binding != null && isAdded()) {
            loadAllData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
