package com.cse2200.maacare.ui.fragments;

import android.app.DatePickerDialog;
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
import com.cse2200.maacare.data.db.entity.BabyFoodLogEntity;
import com.cse2200.maacare.data.db.entity.VaccinationEntity;
import com.cse2200.maacare.data.prefs.PreferenceManager;
import com.cse2200.maacare.data.repository.MaaCareRepository;
import com.cse2200.maacare.databinding.DialogAddBabyFoodBinding;
import com.cse2200.maacare.databinding.FragmentCareAndVaccineBinding;
import com.cse2200.maacare.ui.adapters.BabyFoodLogAdapter;
import com.cse2200.maacare.ui.adapters.VaccinationAdapter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment managing Chronological Child Age Tracking, WHO & EPI Newborn Immunizations
 * (categorized in Weeks & Months with Completed, Due Now, and Upcoming badges),
 * and Age-Appropriate Nutrition Guides & Allergy Logs.
 */
public class CareAndVaccineFragment extends Fragment {

    private FragmentCareAndVaccineBinding binding;
    private MaaCareRepository repository;
    private PreferenceManager preferenceManager;
    private VaccinationAdapter vaccineAdapter;
    private BabyFoodLogAdapter babyFoodAdapter;

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

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

        setupBabyAgeBanner();
        setupToggleSections();
        setupVaccinesRecyclerView();
        setupBabyFoodRecyclerView();

        loadVaccines();
        loadBabyFoodLogs();

        binding.fabAddBabyFood.setOnClickListener(v -> showAddBabyFoodDialog());
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

    private void setupToggleSections() {
        binding.toggleCareSections.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_section_vaccine) {
                    binding.layoutVaccineSection.setVisibility(View.VISIBLE);
                    binding.layoutNutritionGuideSection.setVisibility(View.GONE);
                    binding.layoutFoodSection.setVisibility(View.GONE);
                    binding.fabAddBabyFood.setVisibility(View.GONE);
                } else if (checkedId == R.id.btn_section_nutrition_guide) {
                    binding.layoutVaccineSection.setVisibility(View.GONE);
                    binding.layoutNutritionGuideSection.setVisibility(View.VISIBLE);
                    binding.layoutFoodSection.setVisibility(View.GONE);
                    binding.fabAddBabyFood.setVisibility(View.GONE);
                } else if (checkedId == R.id.btn_section_food) {
                    binding.layoutVaccineSection.setVisibility(View.GONE);
                    binding.layoutNutritionGuideSection.setVisibility(View.GONE);
                    binding.layoutFoodSection.setVisibility(View.VISIBLE);
                    binding.fabAddBabyFood.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupVaccinesRecyclerView() {
        vaccineAdapter = new VaccinationAdapter((vaccine, isDone) -> {
            vaccine.setDone(isDone);
            vaccine.setAdministeredDate(isDone ? "Administered on " + DATE_FMT.format(new Date()) : "Pending");
            repository.updateVaccine(vaccine, new MaaCareRepository.DataCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    loadVaccines();
                }

                @Override
                public void onError(Throwable error) {}
            });
        });
        vaccineAdapter.setBabyAgeDays(preferenceManager.getBabyAgeDays());
        binding.rvVaccinations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvVaccinations.setAdapter(vaccineAdapter);
    }

    private void setupBabyFoodRecyclerView() {
        babyFoodAdapter = new BabyFoodLogAdapter(log -> {
            repository.deleteBabyFoodLog(log, new MaaCareRepository.DataCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Feeding record removed", Toast.LENGTH_SHORT).show();
                            loadBabyFoodLogs();
                        });
                    }
                }

                @Override
                public void onError(Throwable error) {}
            });
        });
        binding.rvBabyFoodLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBabyFoodLogs.setAdapter(babyFoodAdapter);
    }

    private void loadVaccines() {
        int babyDays = preferenceManager.getBabyAgeDays();
        vaccineAdapter.setBabyAgeDays(babyDays);

        repository.getAllVaccines(new MaaCareRepository.DataCallback<List<VaccinationEntity>>() {
            @Override
            public void onSuccess(List<VaccinationEntity> list) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        vaccineAdapter.submitList(list);

                        int done = 0;
                        int dueNow = 0;
                        for (VaccinationEntity v : list) {
                            if (v.isDone()) done++;
                            else if (v.computeStatus(babyDays).equals("DUE NOW")) dueNow++;
                        }
                        binding.tvVaccineProgress.setText(done + " of " + list.size() + " Vaccines Administered • " + dueNow + " Due Now");
                    });
                }
            }

            @Override
            public void onError(Throwable error) {}
        });
    }

    private void loadBabyFoodLogs() {
        repository.getAllBabyFoodLogs(new MaaCareRepository.DataCallback<List<BabyFoodLogEntity>>() {
            @Override
            public void onSuccess(List<BabyFoodLogEntity> list) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        babyFoodAdapter.submitList(list);
                        binding.tvFoodSummary.setText(list.size() + " Feeding and Allergy Records Logged");
                    });
                }
            }

            @Override
            public void onError(Throwable error) {}
        });
    }

    private void showAddBabyFoodDialog() {
        DialogAddBabyFoodBinding dBinding = DialogAddBabyFoodBinding.inflate(LayoutInflater.from(requireContext()));

        new AlertDialog.Builder(requireContext())
                .setView(dBinding.getRoot())
                .setPositiveButton("Save Record", (dialog, which) -> {
                    String food = dBinding.etBabyFoodName.getText() != null ? dBinding.etBabyFoodName.getText().toString().trim() : "";
                    String details = dBinding.etBabyFeedingDetails.getText() != null ? dBinding.etBabyFeedingDetails.getText().toString().trim() : "";
                    String reaction = dBinding.etBabyAllergyReaction.getText() != null ? dBinding.etBabyAllergyReaction.getText().toString().trim() : "None";

                    if (food.isEmpty()) {
                        Toast.makeText(requireContext(), "Food/Milk description required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String category = "0-6 Months (EBF)";
                    int checkedId = dBinding.chipGroupBabyAge.getCheckedChipId();
                    if (checkedId == R.id.chip_stage_6_8) category = "6-8 Months";
                    else if (checkedId == R.id.chip_stage_9_11) category = "9-11 Months";
                    else if (checkedId == R.id.chip_stage_12_24) category = "12-24 Months";

                    BabyFoodLogEntity log = new BabyFoodLogEntity(category, food, details, reaction, System.currentTimeMillis());
                    repository.insertBabyFoodLog(log, new MaaCareRepository.DataCallback<Long>() {
                        @Override
                        public void onSuccess(Long id) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "Feeding Logged", Toast.LENGTH_SHORT).show();
                                    loadBabyFoodLogs();
                                });
                            }
                        }

                        @Override
                        public void onError(Throwable error) {}
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
