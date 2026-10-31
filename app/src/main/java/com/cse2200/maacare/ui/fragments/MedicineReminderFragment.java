package com.cse2200.maacare.ui.fragments;

import android.app.TimePickerDialog;
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
import com.cse2200.maacare.data.db.entity.MedicineEntity;
import com.cse2200.maacare.data.repository.MaaCareRepository;
import com.cse2200.maacare.databinding.DialogAddMedicineBinding;
import com.cse2200.maacare.databinding.FragmentMedicineReminderBinding;
import com.cse2200.maacare.ui.adapters.MedicineAdapter;
import com.cse2200.maacare.util.AlarmScheduler;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Prescribed Medicine & Supplement Reminder Fragment.
 * Implements Room database persistence, AlarmManager scheduling, and DiffUtil updates.
 */
public class MedicineReminderFragment extends Fragment implements MedicineAdapter.OnMedicineActionListener {

    private FragmentMedicineReminderBinding binding;
    private MaaCareRepository repository;
    private MedicineAdapter medicineAdapter;
    private String selectedTimeFormatted = "09:00 AM";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMedicineReminderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new MaaCareRepository(requireContext());

        setupRecyclerView();
        loadMedicines();

        binding.fabAddMedicine.setOnClickListener(v -> showAddMedicineDialog());
    }

    private void setupRecyclerView() {
        medicineAdapter = new MedicineAdapter(this);
        binding.rvMedicines.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMedicines.setAdapter(medicineAdapter);
    }

    private void loadMedicines() {
        repository.getAllMedicines(new MaaCareRepository.DataCallback<List<MedicineEntity>>() {
            @Override
            public void onSuccess(List<MedicineEntity> list) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        medicineAdapter.submitList(list);
                        if (list.isEmpty()) {
                            binding.layoutEmptyMedicines.setVisibility(View.VISIBLE);
                        } else {
                            binding.layoutEmptyMedicines.setVisibility(View.GONE);
                            int takenCount = 0;
                            for (MedicineEntity m : list) {
                                if (m.isTakenToday()) takenCount++;
                            }
                            binding.tvAdherenceStatus.setText(takenCount + " of " + list.size() + " supplements taken today.");
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Failed to load medicines", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void showAddMedicineDialog() {
        DialogAddMedicineBinding dialogBinding = DialogAddMedicineBinding.inflate(LayoutInflater.from(requireContext()));
        selectedTimeFormatted = "09:00 AM";
        dialogBinding.tvSelectedTime.setText("Reminder Time: " + selectedTimeFormatted);

        dialogBinding.btnPickTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(requireContext(), (view, hourOfDay, minuteOfHour) -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(Calendar.HOUR_OF_DAY, hourOfDay);
                chosen.set(Calendar.MINUTE, minuteOfHour);
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                selectedTimeFormatted = sdf.format(chosen.getTime());
                dialogBinding.tvSelectedTime.setText("Reminder Time: " + selectedTimeFormatted);
            }, hour, minute, false);
            timePicker.show();
        });

        new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Add Prescription", (dialog, which) -> {
                    String name = dialogBinding.etMedicineName.getText() != null ? dialogBinding.etMedicineName.getText().toString().trim() : "";
                    String dosage = dialogBinding.etMedicineDosage.getText() != null ? dialogBinding.etMedicineDosage.getText().toString().trim() : "";

                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Medicine name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (dosage.isEmpty()) {
                        dosage = "1 dose as prescribed";
                    }

                    MedicineEntity newMed = new MedicineEntity(name, dosage, selectedTimeFormatted, false, true);
                    repository.insertMedicine(newMed, new MaaCareRepository.DataCallback<Long>() {
                        @Override
                        public void onSuccess(Long id) {
                            newMed.setId(id);
                            AlarmScheduler.scheduleMedicationAlarm(requireContext(), newMed);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "Prescription Added & Alarm Scheduled", Toast.LENGTH_SHORT).show();
                                    loadMedicines();
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
    public void onTakenToggled(MedicineEntity medicine, boolean isTaken) {
        medicine.setTakenToday(isTaken);
        repository.updateMedicine(medicine, new MaaCareRepository.DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                loadMedicines();
            }

            @Override
            public void onError(Throwable error) {}
        });
    }

    @Override
    public void onReminderToggled(MedicineEntity medicine, boolean isEnabled) {
        medicine.setReminderEnabled(isEnabled);
        if (isEnabled) {
            AlarmScheduler.scheduleMedicationAlarm(requireContext(), medicine);
        } else {
            AlarmScheduler.cancelMedicationAlarm(requireContext(), medicine);
        }

        repository.updateMedicine(medicine, new MaaCareRepository.DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        String msg = isEnabled ? "Reminder enabled" : "Reminder disabled";
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        loadMedicines();
                    });
                }
            }

            @Override
            public void onError(Throwable error) {}
        });
    }

    @Override
    public void onDeleteClicked(MedicineEntity medicine) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Prescription")
                .setMessage("Remove " + medicine.getName() + " from scheduled reminders?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    AlarmScheduler.cancelMedicationAlarm(requireContext(), medicine);
                    repository.deleteMedicine(medicine, new MaaCareRepository.DataCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer result) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "Medication removed", Toast.LENGTH_SHORT).show();
                                    loadMedicines();
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
