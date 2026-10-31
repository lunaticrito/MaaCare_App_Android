package com.cse2200.maacare.ui.fragments;

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
import com.cse2200.maacare.data.db.entity.FoodLogEntity;
import com.cse2200.maacare.data.db.entity.VaccinationEntity;
import com.cse2200.maacare.data.repository.MaaCareRepository;
import com.cse2200.maacare.databinding.DialogAddFoodLogBinding;
import com.cse2200.maacare.databinding.FragmentCareAndVaccineBinding;
import com.cse2200.maacare.ui.adapters.FoodLogAdapter;
import com.cse2200.maacare.ui.adapters.VaccinationAdapter;
import java.util.List;

/**
 * Fragment managing Newborn Immunization Schedule (EPI) and Maternal Nutrition Food Logs.
 */
public class CareAndVaccineFragment extends Fragment {

    private FragmentCareAndVaccineBinding binding;
    private MaaCareRepository repository;
    private VaccinationAdapter vaccineAdapter;
    private FoodLogAdapter foodLogAdapter;

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

        setupToggleSections();
        setupVaccinesRecyclerView();
        setupFoodRecyclerView();

        loadVaccines();
        loadFoodLogs();

        binding.fabAddFoodLog.setOnClickListener(v -> showAddFoodLogDialog());
    }

    private void setupToggleSections() {
        binding.toggleCareSections.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_section_vaccine) {
                    binding.layoutVaccineSection.setVisibility(View.VISIBLE);
                    binding.layoutFoodSection.setVisibility(View.GONE);
                    binding.fabAddFoodLog.setVisibility(View.GONE);
                } else if (checkedId == R.id.btn_section_food) {
                    binding.layoutVaccineSection.setVisibility(View.GONE);
                    binding.layoutFoodSection.setVisibility(View.VISIBLE);
                    binding.fabAddFoodLog.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupVaccinesRecyclerView() {
        vaccineAdapter = new VaccinationAdapter((vaccine, isDone) -> {
            vaccine.setDone(isDone);
            vaccine.setAdministeredDate(isDone ? "Administered" : "Pending");
            repository.updateVaccine(vaccine, new MaaCareRepository.DataCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    loadVaccines();
                }

                @Override
                public void onError(Throwable error) {}
            });
        });
        binding.rvVaccinations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvVaccinations.setAdapter(vaccineAdapter);
    }

    private void setupFoodRecyclerView() {
        foodLogAdapter = new FoodLogAdapter(log -> {
            repository.deleteFoodLog(log, new MaaCareRepository.DataCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Meal entry removed", Toast.LENGTH_SHORT).show();
                            loadFoodLogs();
                        });
                    }
                }

                @Override
                public void onError(Throwable error) {}
            });
        });
        binding.rvFoodLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFoodLogs.setAdapter(foodLogAdapter);
    }

    private void loadVaccines() {
        repository.getAllVaccines(new MaaCareRepository.DataCallback<List<VaccinationEntity>>() {
            @Override
            public void onSuccess(List<VaccinationEntity> list) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        vaccineAdapter.submitList(list);
                        int doneCount = 0;
                        for (VaccinationEntity v : list) {
                            if (v.isDone()) doneCount++;
                        }
                        binding.tvVaccineProgress.setText(doneCount + " of " + list.size() + " Newborn Vaccines Administered");
                    });
                }
            }

            @Override
            public void onError(Throwable error) {}
        });
    }

    private void loadFoodLogs() {
        repository.getAllFoodLogs(new MaaCareRepository.DataCallback<List<FoodLogEntity>>() {
            @Override
            public void onSuccess(List<FoodLogEntity> list) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        foodLogAdapter.submitList(list);
                        int totalCalories = 0;
                        double totalProtein = 0;
                        for (FoodLogEntity item : list) {
                            totalCalories += item.getCalories();
                            totalProtein += item.getProteinGrams();
                        }
                        binding.tvFoodSummary.setText("Logged Today: " + totalCalories + " kcal • " + String.format("%.1f", totalProtein) + "g Protein");
                    });
                }
            }

            @Override
            public void onError(Throwable error) {}
        });
    }

    private void showAddFoodLogDialog() {
        DialogAddFoodLogBinding dialogBinding = DialogAddFoodLogBinding.inflate(LayoutInflater.from(requireContext()));

        new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Log Meal", (dialog, which) -> {
                    String name = dialogBinding.etFoodName.getText() != null ? dialogBinding.etFoodName.getText().toString().trim() : "";
                    String calStr = dialogBinding.etFoodCalories.getText() != null ? dialogBinding.etFoodCalories.getText().toString().trim() : "0";
                    String proStr = dialogBinding.etFoodProtein.getText() != null ? dialogBinding.etFoodProtein.getText().toString().trim() : "0";

                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Meal description required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int cal = 0;
                    double pro = 0.0;
                    try {
                        cal = Integer.parseInt(calStr);
                        pro = Double.parseDouble(proStr);
                    } catch (NumberFormatException ignored) {}

                    FoodLogEntity newLog = new FoodLogEntity(name, cal, pro, System.currentTimeMillis());
                    repository.insertFoodLog(newLog, new MaaCareRepository.DataCallback<Long>() {
                        @Override
                        public void onSuccess(Long id) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "Meal Logged", Toast.LENGTH_SHORT).show();
                                    loadFoodLogs();
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
