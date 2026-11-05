package com.cse2200.maacare.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.cse2200.maacare.R;
import com.cse2200.maacare.data.db.entity.BabyEssentialItemEntity;
import com.cse2200.maacare.data.db.entity.DoctorEntity;
import com.cse2200.maacare.data.model.MaternalHealthDataResponse;
import com.cse2200.maacare.data.prefs.PreferenceManager;
import com.cse2200.maacare.data.repository.MaaCareRepository;
import com.cse2200.maacare.databinding.FragmentEmergencyGuideBinding;
import com.cse2200.maacare.ui.adapters.DangerSignAdapter;
import com.cse2200.maacare.ui.adapters.DoctorAdapter;
import com.cse2200.maacare.ui.adapters.EssentialsAdapter;
import java.util.List;

/**
 * Emergency Health Guidance & Doctor Directory Fragment featuring:
 * - One-tap SOS calling via Intent.ACTION_DIAL (999 and primary caregiver)
 * - Automated Maternal Emergency SMS Dispatch with gestational week & blood group
 * - Find A Doctor Near You with live search & specialty filters
 * - Hospital Bag Essentials interactive checklist
 * - Obstetric Danger Signs Checklist & First-Aid Protocols
 */
public class EmergencyGuideFragment extends Fragment {

    private FragmentEmergencyGuideBinding binding;
    private PreferenceManager preferenceManager;
    private MaaCareRepository repository;

    private DoctorAdapter doctorAdapter;
    private EssentialsAdapter essentialsAdapter;
    private DangerSignAdapter dangerSignAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmergencyGuideBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferenceManager = new PreferenceManager(requireContext());
        repository = new MaaCareRepository(requireContext());

        setupEmergencyActionButtons();
        setupDoctorDirectory();
        setupEssentialsChecklist();
        setupDangerSigns();

        loadAllData();
    }

    private void setupEmergencyActionButtons() {
        // 1. One-tap Emergency Calling to 999
        binding.btnSosCallAmbulance.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:999"));
            startActivity(callIntent);
        });

        // 2. One-tap Calling to Caregiver
        binding.btnSosCallCaregiver.setOnClickListener(v -> {
            String phone = preferenceManager.getEmergencyPhone();
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phone));
            startActivity(callIntent);
        });

        // 3. One-tap Automated Emergency SMS
        binding.btnSosSendSms.setOnClickListener(v -> {
            String phone = preferenceManager.getEmergencyPhone();
            String motherName = preferenceManager.getMotherName();
            int week = preferenceManager.getGestationalWeek();
            String blood = preferenceManager.getBloodGroup();

            String message = "EMERGENCY SOS: Maternal emergency assistance needed for "
                    + motherName + " (Gestational Week: " + week
                    + ", Blood Group: " + blood + "). Please assist immediately!";

            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + phone));
            smsIntent.putExtra("sms_body", message);
            try {
                startActivity(smsIntent);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "No SMS app found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDoctorDirectory() {
        doctorAdapter = new DoctorAdapter();
        binding.rvNearestDoctors.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNearestDoctors.setAdapter(doctorAdapter);

        // Search Listener
        binding.etSearchDoctors.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s != null ? s.toString().trim() : "";
                if (query.isEmpty()) {
                    loadDoctors();
                } else {
                    repository.searchDoctors(query, new MaaCareRepository.DataCallback<List<DoctorEntity>>() {
                        @Override
                        public void onSuccess(List<DoctorEntity> list) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> doctorAdapter.setDoctors(list));
                            }
                        }
                        @Override public void onError(Throwable error) {}
                    });
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Specialty Filter Chips
        binding.chipGroupDoctorSpecialty.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_spec_obstetrician) {
                filterDoctorsBySpecialty("Obstetrician");
            } else if (checkedId == R.id.chip_spec_gynecologist) {
                filterDoctorsBySpecialty("Gynecologist");
            } else if (checkedId == R.id.chip_spec_pediatrician) {
                filterDoctorsBySpecialty("Pediatrician");
            } else {
                loadDoctors();
            }
        });
    }

    private void filterDoctorsBySpecialty(String specialty) {
        repository.getDoctorsBySpecialty(specialty, new MaaCareRepository.DataCallback<List<DoctorEntity>>() {
            @Override
            public void onSuccess(List<DoctorEntity> list) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> doctorAdapter.setDoctors(list));
                }
            }
            @Override public void onError(Throwable error) {}
        });
    }

    private void setupEssentialsChecklist() {
        essentialsAdapter = new EssentialsAdapter((item, isChecked) -> {
            repository.updateEssentialChecked(item.getId(), isChecked, new MaaCareRepository.DataCallback<Integer>() {
                @Override public void onSuccess(Integer result) {}
                @Override public void onError(Throwable error) {}
            });
        });
        binding.rvHospitalEssentials.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHospitalEssentials.setAdapter(essentialsAdapter);
    }

    private void setupDangerSigns() {
        dangerSignAdapter = new DangerSignAdapter();
        binding.rvDangerSigns.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDangerSigns.setAdapter(dangerSignAdapter);
    }

    private void loadAllData() {
        loadDoctors();
        loadEssentials();
        loadDangerSigns();
    }

    private void loadDoctors() {
        repository.getAllDoctors(new MaaCareRepository.DataCallback<List<DoctorEntity>>() {
            @Override
            public void onSuccess(List<DoctorEntity> list) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> doctorAdapter.setDoctors(list));
                }
            }
            @Override public void onError(Throwable error) {}
        });
    }

    private void loadEssentials() {
        repository.getAllEssentials(new MaaCareRepository.DataCallback<List<BabyEssentialItemEntity>>() {
            @Override
            public void onSuccess(List<BabyEssentialItemEntity> list) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> essentialsAdapter.setItems(list));
                }
            }
            @Override public void onError(Throwable error) {}
        });
    }

    private void loadDangerSigns() {
        repository.getMaternalHealthData(new MaaCareRepository.DataCallback<MaternalHealthDataResponse>() {
            @Override
            public void onSuccess(MaternalHealthDataResponse result) {
                if (getActivity() != null && result != null) {
                    getActivity().runOnUiThread(() -> dangerSignAdapter.submitList(result.getDangerSigns()));
                }
            }
            @Override public void onError(Throwable error) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
