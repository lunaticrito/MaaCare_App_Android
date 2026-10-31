package com.cse2200.maacare.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.cse2200.maacare.data.model.MaternalHealthDataResponse;
import com.cse2200.maacare.data.prefs.PreferenceManager;
import com.cse2200.maacare.data.repository.MaaCareRepository;
import com.cse2200.maacare.databinding.FragmentEmergencyGuideBinding;
import com.cse2200.maacare.ui.adapters.DangerSignAdapter;
import com.cse2200.maacare.ui.adapters.HospitalAdapter;

/**
 * Emergency Health Guidance Fragment featuring:
 * - One-tap SOS calling via Intent.ACTION_DIAL
 * - Automated Maternal Emergency SMS Dispatch with gestational week & blood group
 * - Obstetric Danger Signs Checklist & First-Aid Protocols
 * - 24/7 Maternal Hospital Directory
 */
public class EmergencyGuideFragment extends Fragment {

    private FragmentEmergencyGuideBinding binding;
    private PreferenceManager preferenceManager;
    private MaaCareRepository repository;
    private DangerSignAdapter dangerSignAdapter;
    private HospitalAdapter hospitalAdapter;

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
        setupRecyclerViews();
        loadEmergencyData();
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

    private void setupRecyclerViews() {
        // Danger Signs
        dangerSignAdapter = new DangerSignAdapter();
        binding.rvDangerSigns.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDangerSigns.setAdapter(dangerSignAdapter);

        // Hospital Directory
        hospitalAdapter = new HospitalAdapter(contact -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + contact.getPhoneNumber()));
            startActivity(callIntent);
        });
        binding.rvHospitalDirectory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHospitalDirectory.setAdapter(hospitalAdapter);
    }

    private void loadEmergencyData() {
        repository.getMaternalHealthData(new MaaCareRepository.DataCallback<MaternalHealthDataResponse>() {
            @Override
            public void onSuccess(MaternalHealthDataResponse result) {
                if (getActivity() != null && result != null) {
                    getActivity().runOnUiThread(() -> {
                        dangerSignAdapter.submitList(result.getDangerSigns());
                        hospitalAdapter.submitList(result.getEmergencyDirectory());
                    });
                }
            }

            @Override
            public void onError(Throwable error) {
                // Handled gracefully by local assets fallback in repository
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
