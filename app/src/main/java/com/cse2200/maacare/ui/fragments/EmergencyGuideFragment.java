package com.cse2200.maacare.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.cse2200.maacare.data.db.entity.BabyEssentialItemEntity;
import com.cse2200.maacare.data.db.entity.DoctorEntity;
import com.cse2200.maacare.data.model.DangerSign;
import com.cse2200.maacare.data.model.MaternalHealthDataResponse;
import com.cse2200.maacare.data.prefs.PreferenceManager;
import com.cse2200.maacare.data.repository.MaaCareRepository;
import com.cse2200.maacare.databinding.FragmentEmergencyGuideBinding;
import com.cse2200.maacare.firebase.FirebaseManager;
import com.cse2200.maacare.ui.adapters.DangerSignAdapter;
import com.cse2200.maacare.ui.adapters.DoctorAdapter;
import com.cse2200.maacare.ui.adapters.EssentialsAdapter;
import com.cse2200.maacare.util.LocaleHelper;
import com.google.android.material.chip.Chip;
import java.util.List;

/**
 * Bilingual Emergency Health Guidance & 64-District Bangladesh Doctor Directory Fragment:
 * - One-tap SOS calling via Intent.ACTION_DIAL (999 and primary caregiver)
 * - Automated Maternal Emergency SMS Dispatch
 * - Device ContentProvider Consumption (ContactsContract) + Set Doctor as Emergency Contact
 * - Complete 129-Doctor Directory filterable by 8 Divisions, 64 Districts, Specialty & Search
 * - Hospital Bag Essentials checklist & Bilingual Obstetric Danger Signs (JSON + XML Protocols)
 */
public class EmergencyGuideFragment extends Fragment implements DoctorAdapter.OnDoctorActionListener {

    private static final int REQUEST_CODE_PICK_CONTACT = 202;
    private static final int PERMISSION_REQUEST_CONTACTS = 203;

    private FragmentEmergencyGuideBinding binding;
    private PreferenceManager preferenceManager;
    private MaaCareRepository repository;

    private DoctorAdapter doctorAdapter;
    private EssentialsAdapter essentialsAdapter;
    private DangerSignAdapter dangerSignAdapter;

    private String selectedDivision = "";
    private String selectedDistrict = "";
    private String selectedSpecialtyCategory = "";
    private String searchQuery = "";

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
        updateCaregiverContactDisplay();
        setupDoctorDirectory();
        setupEssentialsChecklist();
        setupDangerSigns();

        loadAllData();
    }

    private void updateCaregiverContactDisplay() {
        String name = preferenceManager.getEmergencyContactName();
        String phone = preferenceManager.getEmergencyPhone();
        if (LocaleHelper.isBangla(requireContext())) {
            String displayName = "Primary Caregiver".equalsIgnoreCase(name) ? "প্রাথমিক অভিভাবক" : name;
            binding.tvCaregiverContactDisplay.setText("জরুরি অভিভাবক: " + displayName + " (" + LocaleHelper.toBanglaDigits(phone) + ")");
        } else {
            binding.tvCaregiverContactDisplay.setText("Caregiver: " + name + " (" + phone + ")");
        }
    }

    private void setupEmergencyActionButtons() {
        binding.btnSosCallAmbulance.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:999"));
            startActivity(callIntent);
        });

        binding.btnSosCallCaregiver.setOnClickListener(v -> {
            String phone = preferenceManager.getEmergencyPhone();
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phone));
            startActivity(callIntent);
        });

        binding.btnImportContactCaregiver.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED) {
                launchContactPicker();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_CONTACTS);
            }
        });

        binding.btnSosSendSms.setOnClickListener(v -> {
            String phone = preferenceManager.getEmergencyPhone();
            String motherName = preferenceManager.getMotherName();
            int week = preferenceManager.getGestationalWeek();
            String blood = preferenceManager.getBloodGroup();

            String message = "EMERGENCY SOS: Maternal emergency assistance needed for "
                    + motherName + " (Gestational Week: " + week
                    + ", Blood Group: " + blood + "). Please assist immediately!";

            String uid = preferenceManager.getFirebaseUid();
            FirebaseManager.getInstance(requireContext()).syncRealtimeSosAlert(uid, true, message);

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

    private void launchContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchContactPicker();
            } else {
                Toast.makeText(requireContext(), "Permission needed to import contact.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_CONTACT && resultCode == Activity.RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            if (contactUri != null) {
                String[] projection = new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                };
                try (Cursor cursor = requireContext().getContentResolver().query(contactUri, projection, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        int numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                        String name = nameIdx != -1 ? cursor.getString(nameIdx) : "Caregiver";
                        String phone = numberIdx != -1 ? cursor.getString(numberIdx) : "";
                        phone = phone.replaceAll("\\s+", "");

                        preferenceManager.setEmergencyContactName(name);
                        preferenceManager.setEmergencyPhone(phone);
                        updateCaregiverContactDisplay();

                        Toast.makeText(requireContext(), "Imported Caregiver: " + name + " (" + phone + ")", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Failed importing contact: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onSetEmergencyDoctor(DoctorEntity doctor) {
        preferenceManager.setEmergencyContactName(doctor.getName());
        preferenceManager.setEmergencyPhone(doctor.getPhone());
        updateCaregiverContactDisplay();

        boolean bn = LocaleHelper.isBangla(requireContext());
        String msg = bn
                ? "জরুরি যোগাযোগ হিসেবে সেট করা হয়েছে: " + doctor.getName()
                : "Set as Primary Emergency Contact: " + doctor.getName();
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void setupDoctorDirectory() {
        doctorAdapter = new DoctorAdapter(this);
        binding.rvNearestDoctors.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNearestDoctors.setAdapter(doctorAdapter);

        // 1. Search Box Listener
        binding.etSearchDoctors.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s != null ? s.toString().trim() : "";
                applyDoctorFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 2. Division Filter Chips (8 Divisions)
        binding.chipGroupDoctorDivision.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_div_dhaka) selectedDivision = "Dhaka";
            else if (checkedId == R.id.chip_div_chattogram) selectedDivision = "Chattogram";
            else if (checkedId == R.id.chip_div_khulna) selectedDivision = "Khulna";
            else if (checkedId == R.id.chip_div_rajshahi) selectedDivision = "Rajshahi";
            else if (checkedId == R.id.chip_div_sylhet) selectedDivision = "Sylhet";
            else if (checkedId == R.id.chip_div_barishal) selectedDivision = "Barishal";
            else if (checkedId == R.id.chip_div_rangpur) selectedDivision = "Rangpur";
            else if (checkedId == R.id.chip_div_mymensingh) selectedDivision = "Mymensingh";
            else selectedDivision = "";

            selectedDistrict = "";
            loadDistrictChipsForDivision(selectedDivision);
            applyDoctorFilters();
        });

        // 3. Specialty Filter Chips
        binding.chipGroupDoctorSpecialty.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_spec_obs_gynae) {
                selectedSpecialtyCategory = "Obs & Gynae";
            } else if (checkedId == R.id.chip_spec_pediatrician) {
                selectedSpecialtyCategory = "Pediatrics";
            } else {
                selectedSpecialtyCategory = "";
            }
            applyDoctorFilters();
        });

        loadDistrictChipsForDivision("");
    }

    private void loadDistrictChipsForDivision(String division) {
        repository.getDistrictsByDivision(division, new MaaCareRepository.DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> districts) {
                if (getActivity() != null && binding != null) {
                    getActivity().runOnUiThread(() -> populateDistrictChips(districts));
                }
            }
            @Override public void onError(Throwable error) {}
        });
    }

    private void populateDistrictChips(List<String> districts) {
        binding.chipGroupDoctorDistrict.setOnCheckedChangeListener(null);
        binding.chipGroupDoctorDistrict.removeAllViews();

        Chip firstAll = createFilterChip(getString(R.string.filter_all_districts), "");
        firstAll.setId(View.generateViewId());
        firstAll.setChecked(true);
        binding.chipGroupDoctorDistrict.addView(firstAll);

        if (districts != null) {
            for (String dist : districts) {
                String label = LocaleHelper.translateDistrict(requireContext(), dist);
                Chip chip = createFilterChip(label, dist);
                chip.setId(View.generateViewId());
                binding.chipGroupDoctorDistrict.addView(chip);
            }
        }

        binding.chipGroupDoctorDistrict.setOnCheckedChangeListener((group, checkedId) -> {
            View checkedView = group.findViewById(checkedId);
            if (checkedView instanceof Chip && checkedView.getTag() instanceof String) {
                selectedDistrict = (String) checkedView.getTag();
            } else {
                selectedDistrict = "";
            }
            applyDoctorFilters();
        });
    }

    private Chip createFilterChip(String label, String rawDistrictTag) {
        Chip chip = new Chip(new androidx.appcompat.view.ContextThemeWrapper(requireContext(), R.style.Widget_MaaCare_Chip));
        chip.setText(label);
        chip.setTag(rawDistrictTag);
        chip.setCheckable(true);
        chip.setChipBackgroundColorResource(R.color.selector_chip_bg);
        chip.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.selector_chip_text));
        chip.setChipStrokeColorResource(R.color.selector_chip_stroke);
        chip.setChipStrokeWidth(getResources().getDisplayMetrics().density);
        return chip;
    }

    private void applyDoctorFilters() {
        repository.filterDoctors(selectedDivision, selectedDistrict, selectedSpecialtyCategory, searchQuery,
                new MaaCareRepository.DataCallback<List<DoctorEntity>>() {
                    @Override
                    public void onSuccess(List<DoctorEntity> list) {
                        if (getActivity() != null && binding != null) {
                            getActivity().runOnUiThread(() -> {
                                doctorAdapter.setDoctors(list);
                                int count = list != null ? list.size() : 0;
                                boolean bn = LocaleHelper.isBangla(requireContext());
                                if (bn) {
                                    binding.tvDoctorResultCount.setText("মোট " + LocaleHelper.toBanglaDigits(String.valueOf(count)) + " জন বিশেষজ্ঞ ডাক্তার পাওয়া গেছে");
                                } else {
                                    binding.tvDoctorResultCount.setText("Showing " + count + " verified specialists");
                                }
                            });
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
        applyDoctorFilters();
        loadEssentials();
        loadDangerSigns();
    }

    private void loadEssentials() {
        repository.getAllEssentials(new MaaCareRepository.DataCallback<List<BabyEssentialItemEntity>>() {
            @Override
            public void onSuccess(List<BabyEssentialItemEntity> list) {
                if (getActivity() != null && binding != null) {
                    getActivity().runOnUiThread(() -> essentialsAdapter.setItems(list));
                }
            }
            @Override public void onError(Throwable error) {}
        });
    }

    private void loadDangerSigns() {
        // Parse local XML emergency protocols first (XmlPullParser) and merge/update with API JSON
        repository.getEmergencyProtocolsFromXml(new MaaCareRepository.DataCallback<List<DangerSign>>() {
            @Override
            public void onSuccess(List<DangerSign> xmlProtocols) {
                if (getActivity() != null && binding != null && xmlProtocols != null && !xmlProtocols.isEmpty()) {
                    getActivity().runOnUiThread(() -> dangerSignAdapter.submitList(xmlProtocols));
                }
            }
            @Override public void onError(Throwable error) {}
        });

        repository.getMaternalHealthData(new MaaCareRepository.DataCallback<MaternalHealthDataResponse>() {
            @Override
            public void onSuccess(MaternalHealthDataResponse result) {
                if (getActivity() != null && binding != null && result != null
                        && result.getDangerSigns() != null && !result.getDangerSigns().isEmpty()) {
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
