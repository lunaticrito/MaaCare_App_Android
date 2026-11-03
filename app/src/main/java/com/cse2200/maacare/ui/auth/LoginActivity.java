package com.cse2200.maacare.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.cse2200.maacare.data.db.DatabaseInitializer;
import com.cse2200.maacare.data.db.entity.UserEntity;
import com.cse2200.maacare.data.prefs.PreferenceManager;
import com.cse2200.maacare.data.repository.MaaCareRepository;
import com.cse2200.maacare.databinding.ActivityLoginBinding;
import com.cse2200.maacare.ui.MainActivity;

/**
 * User Authentication screen supporting credential sign-in and instant Demo Login.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private MaaCareRepository repository;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new MaaCareRepository(this);
        preferenceManager = new PreferenceManager(this);

        // Pre-fill demo credentials for convenience
        binding.etLoginIdentifier.setText(DatabaseInitializer.DEMO_EMAIL);
        binding.etLoginPassword.setText(DatabaseInitializer.DEMO_PASSWORD);

        binding.btnLoginSubmit.setOnClickListener(v -> performLogin());

        binding.btnQuickDemoLogin.setOnClickListener(v -> performQuickDemoLogin());

        binding.tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void performLogin() {
        String identifier = binding.etLoginIdentifier.getText() != null
                ? binding.etLoginIdentifier.getText().toString().trim() : "";
        String password = binding.etLoginPassword.getText() != null
                ? binding.etLoginPassword.getText().toString().trim() : "";

        if (identifier.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email/phone and password", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.login(identifier, password, new MaaCareRepository.DataCallback<UserEntity>() {
            @Override
            public void onSuccess(UserEntity user) {
                runOnUiThread(() -> {
                    if (user != null) {
                        preferenceManager.saveUserSession(
                                user.getId(),
                                user.getName(),
                                user.getEmailOrPhone(),
                                user.getLmpDateMillis(),
                                user.getBabyBirthMillis(),
                                user.getBloodGroup(),
                                user.getEmergencyPhone()
                        );
                        Toast.makeText(LoginActivity.this, "Welcome, " + user.getName(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid credentials. Try quick demo login.", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(Throwable error) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login error: " + error.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void performQuickDemoLogin() {
        // Anna is 30 weeks pregnant (LMP set to 206 days ago = Week 30, Day 3)
        long now = System.currentTimeMillis();
        long lmpAnna = now - (206L * 86400000L);
        long babyBirthAnna = now - (56L * 86400000L);

        preferenceManager.saveUserSession(
                1L,
                "Anna",
                DatabaseInitializer.DEMO_EMAIL,
                lmpAnna,
                babyBirthAnna,
                "O+",
                "+8801711223344"
        );

        Toast.makeText(this, "Logged in as Demo User: Anna (Week 30 Pregnant)", Toast.LENGTH_LONG).show();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
