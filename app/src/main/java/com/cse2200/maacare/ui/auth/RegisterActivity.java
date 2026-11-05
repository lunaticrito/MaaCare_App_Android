package com.cse2200.maacare.ui.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.cse2200.maacare.data.db.entity.UserEntity;
import com.cse2200.maacare.data.prefs.PreferenceManager;
import com.cse2200.maacare.data.repository.MaaCareRepository;
import com.cse2200.maacare.databinding.ActivityRegisterBinding;
import com.cse2200.maacare.ui.BaseActivity;
import com.cse2200.maacare.ui.MainActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Registration Activity with calendar date selection for Last Menstrual Period (LMP)
 * and automatic derivation of gestational timeline.
 */
public class RegisterActivity extends BaseActivity {

    private ActivityRegisterBinding binding;
    private MaaCareRepository repository;
    private PreferenceManager preferenceManager;

    private long selectedLmpMillis = 0;
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new MaaCareRepository(this);
        preferenceManager = new PreferenceManager(this);

        // Default: 20 weeks pregnant if not chosen
        selectedLmpMillis = System.currentTimeMillis() - (140L * 86400000L);
        updateLmpPreview(selectedLmpMillis);

        binding.btnPickLmpDate.setOnClickListener(v -> showDatePicker());

        binding.btnRegisterSubmit.setOnClickListener(v -> performRegistration());

        binding.tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedLmpMillis);

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(year, month, dayOfMonth, 0, 0, 0);
                    selectedLmpMillis = picked.getTimeInMillis();
                    updateLmpPreview(selectedLmpMillis);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        // LMP cannot be in the future
        picker.getDatePicker().setMaxDate(System.currentTimeMillis());
        picker.show();
    }

    private void updateLmpPreview(long lmpMillis) {
        long elapsedDays = Math.max(0, (System.currentTimeMillis() - lmpMillis) / 86400000L);
        int currentWeek = (int) (elapsedDays / 7) + 1;
        int dayOfWeek = (int) (elapsedDays % 7);

        long eddMillis = lmpMillis + (280L * 86400000L);

        binding.tvSelectedLmpDisplay.setText("LMP: " + DATE_FMT.format(new Date(lmpMillis))
                + " (Week " + currentWeek + ", Day " + dayOfWeek + ")");
        binding.tvComputedEddPreview.setText("Estimated Due Date: " + DATE_FMT.format(new Date(eddMillis)));
    }

    private void performRegistration() {
        String name = binding.etRegName.getText() != null ? binding.etRegName.getText().toString().trim() : "";
        String identifier = binding.etRegIdentifier.getText() != null ? binding.etRegIdentifier.getText().toString().trim() : "";
        String password = binding.etRegPassword.getText() != null ? binding.etRegPassword.getText().toString().trim() : "";
        String blood = binding.etRegBloodGroup.getText() != null ? binding.etRegBloodGroup.getText().toString().trim() : "O+";
        String phone = binding.etRegEmergencyPhone.getText() != null ? binding.etRegEmergencyPhone.getText().toString().trim() : "";

        if (name.isEmpty() || identifier.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Child birth baseline for newborn care module
        long babyBirthMillis = System.currentTimeMillis() - (56L * 86400000L);

        UserEntity user = new UserEntity(name, identifier, password, selectedLmpMillis, babyBirthMillis, blood, phone);
        repository.registerUser(user, new MaaCareRepository.DataCallback<Long>() {
            @Override
            public void onSuccess(Long userId) {
                runOnUiThread(() -> {
                    preferenceManager.saveUserSession(userId, name, identifier, selectedLmpMillis, babyBirthMillis, blood, phone);
                    Toast.makeText(RegisterActivity.this, "Profile Registered Successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(Throwable error) {
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Registration failed: " + error.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}
