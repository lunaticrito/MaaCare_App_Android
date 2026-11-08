package com.cse2200.maacare.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.cse2200.maacare.data.db.DatabaseInitializer;
import com.cse2200.maacare.data.db.entity.UserEntity;
import com.cse2200.maacare.data.prefs.PreferenceManager;
import com.cse2200.maacare.data.repository.MaaCareRepository;
import com.cse2200.maacare.databinding.ActivityLoginBinding;
import com.cse2200.maacare.firebase.FirebaseManager;
import com.cse2200.maacare.ui.BaseActivity;
import com.cse2200.maacare.ui.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * User Authentication screen supporting Firebase Auth, Room credentials, and instant Offline Demo Login.
 */
public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    private ActivityLoginBinding binding;
    private MaaCareRepository repository;
    private PreferenceManager preferenceManager;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new MaaCareRepository(this);
        preferenceManager = new PreferenceManager(this);
        firebaseManager = FirebaseManager.getInstance(this);

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

        FirebaseAuth auth = firebaseManager.getAuth();
        if (firebaseManager.isFirebaseAvailable() && auth != null && identifier.contains("@")) {
            // Attempt cloud authentication with Firebase
            auth.signInWithEmailAndPassword(identifier, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful() && auth.getCurrentUser() != null) {
                            FirebaseUser fbUser = auth.getCurrentUser();
                            preferenceManager.setFirebaseUid(fbUser.getUid());
                            Log.i(TAG, "Firebase Auth sign-in successful: " + fbUser.getUid());
                            loginLocalRoomFallback(identifier, password, true);
                        } else {
                            Log.w(TAG, "Firebase Auth failed, falling back to local database: "
                                    + (task.getException() != null ? task.getException().getMessage() : ""));
                            loginLocalRoomFallback(identifier, password, false);
                        }
                    });
        } else {
            // Offline or phone credential login via Room
            loginLocalRoomFallback(identifier, password, false);
        }
    }

    private void loginLocalRoomFallback(String identifier, String password, boolean cloudSynced) {
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
                        String statusMsg = cloudSynced
                                ? "Cloud & Local Sync Active • Welcome, " + user.getName()
                                : "Offline Local Mode • Welcome, " + user.getName();
                        Toast.makeText(LoginActivity.this, statusMsg, Toast.LENGTH_SHORT).show();
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

        preferenceManager.setFirebaseUid("demo_anna_uid");
        preferenceManager.saveUserSession(
                1L,
                "Anna",
                DatabaseInitializer.DEMO_EMAIL,
                lmpAnna,
                babyBirthAnna,
                "O+",
                "+8801711223344"
        );

        Toast.makeText(this, "Logged in as Anna (Demo Profile • Week 30)", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
