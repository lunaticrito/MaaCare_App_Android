package com.cse2200.maacare.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.cse2200.maacare.data.db.entity.HydrationLogEntity;
import com.cse2200.maacare.data.model.MaternalHealthDataResponse;
import com.cse2200.maacare.data.prefs.PreferenceManager;
import com.cse2200.maacare.data.repository.MaaCareRepository;
import com.cse2200.maacare.databinding.DialogEditHydrationBinding;
import com.cse2200.maacare.databinding.DialogEditProfileBinding;
import com.cse2200.maacare.databinding.FragmentHomeTrackerBinding;
import com.cse2200.maacare.firebase.FirebaseManager;
import com.cse2200.maacare.service.MaternalHealthMonitorService;
import com.cse2200.maacare.ui.adapters.FetalMilestoneAdapter;
import com.cse2200.maacare.ui.auth.LoginActivity;
import com.cse2200.maacare.util.LocaleHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Week-by-Week Pregnancy Tracker Fragment featuring:
 * - Deterministic, clock-driven gestational timeline engine (calculated from LMP)
 * - Custom circular arc gauge (PregnancyProgressArcView)
 * - Device MediaStore ContentProvider: Ultrasound & Baby milestone photo picker
 * - Firebase Realtime Database Hydration Sync & Cloud Storage uploads
 * - Fully editable daily hydration controller & foreground service synchronization
 * - RecyclerView for weekly fetal development milestones
 */
public class HomeTrackerFragment extends Fragment {

    private static final int REQUEST_CODE_PICK_PHOTO = 301;
    private static final int PERMISSION_REQUEST_STORAGE = 302;

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
        setupPhotoMemory();
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

        if (LocaleHelper.isBangla(requireContext())) {
            String displayName = "Anna".equalsIgnoreCase(name) ? "অ্যানা" : name;
            binding.tvMotherName.setText("হ্যালো, " + displayName);
            binding.tvProfileSubtext.setText(LocaleHelper.toBanglaDigits(
                    "সপ্তাহ " + week + ", দিন " + day + " • রক্তের গ্রুপ: " + blood + " • প্রসবের বাকি " + daysLeft + " দিন"));
            binding.tvCountdownEdd.setText(LocaleHelper.toBanglaDigits(
                    "প্রসবের আনুমানিক বাকি " + daysLeft + " দিন"));
        } else {
            binding.tvMotherName.setText("Hello, " + name);
            binding.tvProfileSubtext.setText("Week " + week + ", Day " + day + " • Blood: " + blood + " • " + daysLeft + " Days to EDD");
            binding.tvCountdownEdd.setText(daysLeft + " Days to Estimated Delivery");
        }

        // Update Custom View Arc
        binding.arcPregnancyProgress.setProgress(week, daysLeft);
    }

    // =========================================================================
    // Device MediaStore ContentProvider (Ultrasound & Bump Memory Photo)
    // =========================================================================

    private void setupPhotoMemory() {
        loadPersistedPhotoMemory();

        View.OnClickListener pickListener = v -> checkStoragePermissionAndPick();
        binding.btnPickUltrasoundPhoto.setOnClickListener(pickListener);
        binding.ivUltrasoundPhoto.setOnClickListener(pickListener);
    }

    private void checkStoragePermissionAndPick() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            launchGalleryPicker();
        } else {
            requestPermissions(new String[]{permission}, PERMISSION_REQUEST_STORAGE);
        }
    }

    private void launchGalleryPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchGalleryPicker();
            } else {
                Toast.makeText(requireContext(), "Permission needed to select photo from gallery.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                processSelectedImageUri(imageUri);
            }
        }
    }

    private void processSelectedImageUri(Uri imageUri) {
        // Query metadata from MediaStore via ContentResolver
        String fileName = "Ultrasound_Scan.jpg";
        long fileSize = 0;

        String[] projection = new String[]{
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE
        };

        try (Cursor cursor = requireContext().getContentResolver().query(imageUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int sizeIdx = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);

                if (nameIdx != -1) fileName = cursor.getString(nameIdx);
                if (sizeIdx != -1) fileSize = cursor.getLong(sizeIdx);
            }
        } catch (Exception e) {
            fileName = "Memory_Photo.jpg";
        }

        // Copy file to internal app storage for immediate offline display
        File destFile = new File(requireContext().getFilesDir(), "ultrasound_memory.jpg");
        try (InputStream in = requireContext().getContentResolver().openInputStream(imageUri);
             OutputStream out = new FileOutputStream(destFile)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            // Display in ShapeableImageView
            Bitmap bitmap = BitmapFactory.decodeFile(destFile.getAbsolutePath());
            if (bitmap != null) {
                binding.ivUltrasoundPhoto.setImageBitmap(bitmap);
            }

            preferenceManager.setPhotoMemoryUri(destFile.getAbsolutePath());

            long kbSize = fileSize > 0 ? fileSize / 1024 : destFile.length() / 1024;
            String metaInfo = "MediaStore: " + fileName + " (" + kbSize + " KB) • Saved";
            binding.tvPhotoFileInfo.setText(metaInfo);

            Toast.makeText(requireContext(), "Photo saved locally. Syncing with cloud storage...", Toast.LENGTH_SHORT).show();

            // Cloud Backup to Firebase Storage & Firestore
            String uid = preferenceManager.getFirebaseUid();
            String photoId = "ultrasound_w" + preferenceManager.getCurrentWeek() + "_" + System.currentTimeMillis();
            final String savedFileName = fileName;
            final long savedKbSize = kbSize;
            FirebaseManager.getInstance(requireContext()).uploadMilestonePhoto(uid, photoId, destFile, new FirebaseManager.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    FirebaseManager.getInstance(requireContext()).backupPhotoMemoryMetadata(
                            uid, photoId, "Week " + preferenceManager.getCurrentWeek() + " Ultrasound",
                            destFile.getAbsolutePath(), downloadUrl, System.currentTimeMillis()
                    );
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            binding.tvPhotoFileInfo.setText("Cloud Backed: " + savedFileName + " (" + savedKbSize + " KB) ☁️");
                        });
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    // Stays offline safe
                }
            });

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to save photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPersistedPhotoMemory() {
        String savedPath = preferenceManager.getPhotoMemoryUri();
        if (savedPath != null) {
            File photoFile = new File(savedPath);
            if (photoFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                if (bitmap != null) {
                    binding.ivUltrasoundPhoto.setImageBitmap(bitmap);
                    binding.tvPhotoFileInfo.setText("Saved Memory: " + photoFile.getName() + " (" + (photoFile.length() / 1024) + " KB)");
                }
            }
        }
    }

    // =========================================================================
    // Hydration Controller
    // =========================================================================

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

        // Sync with Firebase Realtime Database
        String uid = preferenceManager.getFirebaseUid();
        FirebaseManager.getInstance(requireContext()).syncRealtimeHydration(
                uid,
                preferenceManager.getTodayWaterIntakeMl(),
                preferenceManager.getDailyWaterGoalMl()
        );

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
        int percent = Math.min(100, (int) (((float) intake / (float) goal) * 100));

        if (LocaleHelper.isBangla(requireContext())) {
            String raw = getString(R.string.hydration_count_format, intake, goal, percent);
            binding.tvWaterCount.setText(LocaleHelper.toBanglaDigits(raw));
        } else {
            binding.tvWaterCount.setText(String.format(Locale.getDefault(), "%d mL / %d mL (%d%%)", intake, goal, percent));
        }
        binding.pbWaterProgress.setProgress(percent);
    }

    private void updateServiceButtonState() {
        if (preferenceManager.isMonitorActive()) {
            binding.btnToggleService.setText(R.string.btn_stop_monitor);
            binding.btnToggleService.setTextColor(ContextCompat.getColor(requireContext(), R.color.emergency_red));
            binding.btnToggleService.setStrokeColorResource(R.color.emergency_red);
        } else {
            binding.btnToggleService.setText(R.string.btn_start_monitor);
            binding.btnToggleService.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary_dark));
            binding.btnToggleService.setStrokeColorResource(R.color.secondary);
        }
    }

    private void setupMilestonesRecyclerView() {
        milestoneAdapter = new FetalMilestoneAdapter();
        binding.rvFetalMilestones.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFetalMilestones.setAdapter(milestoneAdapter);
    }

    private void loadMilestoneData() {
        repository.getMaternalHealthData(new MaaCareRepository.DataCallback<MaternalHealthDataResponse>() {
            @Override
            public void onSuccess(MaternalHealthDataResponse result) {
                if (getActivity() != null && result != null) {
                    getActivity().runOnUiThread(() -> milestoneAdapter.submitList(result.getFetalMilestones()));
                }
            }

            @Override
            public void onError(Throwable error) {}
        });
    }

    private void showEditHydrationDialog() {
        DialogEditHydrationBinding dialogBinding = DialogEditHydrationBinding.inflate(getLayoutInflater());
        dialogBinding.etHydrationGoal.setText(String.valueOf(preferenceManager.getDailyWaterGoalMl()));
        dialogBinding.etHydrationVolume.setText(String.valueOf(preferenceManager.getTodayWaterIntakeMl()));

        new AlertDialog.Builder(requireContext())
                .setTitle("Update Hydration Metrics")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save", (dialog, which) -> {
                    String goalStr = dialogBinding.etHydrationGoal.getText() != null ? dialogBinding.etHydrationGoal.getText().toString().trim() : "";
                    String currentStr = dialogBinding.etHydrationVolume.getText() != null ? dialogBinding.etHydrationVolume.getText().toString().trim() : "";

                    if (!goalStr.isEmpty()) {
                        preferenceManager.setDailyWaterGoalMl(Integer.parseInt(goalStr));
                    }
                    if (!currentStr.isEmpty()) {
                        preferenceManager.setTodayWaterIntakeMl(Integer.parseInt(currentStr));
                    }
                    updateHydrationUI();

                    // Sync with Firebase Realtime Database
                    String uid = preferenceManager.getFirebaseUid();
                    FirebaseManager.getInstance(requireContext()).syncRealtimeHydration(
                            uid,
                            preferenceManager.getTodayWaterIntakeMl(),
                            preferenceManager.getDailyWaterGoalMl()
                    );

                    Toast.makeText(requireContext(), "Hydration updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditProfileDialog() {
        DialogEditProfileBinding dialogBinding = DialogEditProfileBinding.inflate(getLayoutInflater());

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
                .setTitle("Edit Maternal Profile")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save", (d, which) -> {
                    String name = dialogBinding.etProfileName.getText() != null ? dialogBinding.etProfileName.getText().toString().trim() : "";
                    String blood = dialogBinding.etProfileBloodGroup.getText() != null ? dialogBinding.etProfileBloodGroup.getText().toString().trim() : "";
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

    public void refreshData() {
        if (binding != null && isAdded()) {
            updateProfileAndArcUI();
            updateHydrationUI();
            loadPersistedPhotoMemory();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(MaternalHealthMonitorService.BROADCAST_WATER_UPDATED);
        requireContext().registerReceiver(waterUpdateReceiver, filter);
        updateProfileAndArcUI();
        updateHydrationUI();
        updateServiceButtonState();
        loadPersistedPhotoMemory();
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
