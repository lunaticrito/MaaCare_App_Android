package com.cse2200.maacare.firebase;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.cse2200.maacare.data.db.entity.BabyCareTimelineEntity;
import com.cse2200.maacare.data.db.entity.MedicineEntity;
import com.cse2200.maacare.data.db.entity.VaccinationEntity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Robust wrapper for Firebase Suite integration (Auth, Realtime Database, Cloud Firestore, Storage)
 * featuring fail-safe initialization and zero-crash fallback to local SQLite & SharedPreferences.
 */
public class FirebaseManager {

    private static final String TAG = "FirebaseManager";
    private static volatile FirebaseManager instance;

    private boolean isAvailable = false;
    private FirebaseAuth auth;
    private FirebaseDatabase realtimeDb;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(Exception e);
    }

    public interface VitalsListener {
        void onVitalsReceived(Map<String, Object> vitalsData);
    }

    private FirebaseManager(Context context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context);
            }
            auth = FirebaseAuth.getInstance();
            realtimeDb = FirebaseDatabase.getInstance();
            firestore = FirebaseFirestore.getInstance();
            storage = FirebaseStorage.getInstance();
            isAvailable = true;
            Log.i(TAG, "Firebase Suite initialized successfully.");
        } catch (Exception e) {
            isAvailable = false;
            Log.w(TAG, "Firebase unavailable or running in offline mode: " + e.getMessage());
        }
    }

    public static FirebaseManager getInstance(Context context) {
        if (instance == null) {
            synchronized (FirebaseManager.class) {
                if (instance == null) {
                    instance = new FirebaseManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public boolean isFirebaseAvailable() {
        return isAvailable;
    }

    @Nullable
    public FirebaseAuth getAuth() {
        return isAvailable ? auth : null;
    }

    @Nullable
    public FirebaseDatabase getRealtimeDatabase() {
        return isAvailable ? realtimeDb : null;
    }

    @Nullable
    public FirebaseFirestore getFirestore() {
        return isAvailable ? firestore : null;
    }

    @Nullable
    public FirebaseStorage getStorage() {
        return isAvailable ? storage : null;
    }

    // =========================================================================
    // Realtime Database Sync (Hydration, Baby Vitals, SOS Alerts)
    // =========================================================================

    public void syncRealtimeHydration(String uid, int intakeMl, int goalMl) {
        if (!isAvailable || realtimeDb == null || uid == null) return;
        try {
            DatabaseReference ref = realtimeDb.getReference("users")
                    .child(uid)
                    .child("realtime_monitoring")
                    .child("hydration");

            Map<String, Object> data = new HashMap<>();
            data.put("current_intake_ml", intakeMl);
            data.put("daily_goal_ml", goalMl);
            data.put("last_updated_timestamp", System.currentTimeMillis());
            ref.setValue(data);
        } catch (Exception e) {
            Log.w(TAG, "Realtime DB hydration sync bypassed: " + e.getMessage());
        }
    }

    public void syncRealtimeBabyVitals(String uid, boolean isSick, double temp, int pulse, int spo2, String notes) {
        if (!isAvailable || realtimeDb == null || uid == null) return;
        try {
            DatabaseReference ref = realtimeDb.getReference("users")
                    .child(uid)
                    .child("realtime_monitoring")
                    .child("baby_vitals");

            Map<String, Object> data = new HashMap<>();
            data.put("is_sick", isSick);
            data.put("temperature_f", temp);
            data.put("pulse_bpm", pulse);
            data.put("spo2_percent", spo2);
            data.put("symptoms_notes", notes);
            data.put("recorded_at", System.currentTimeMillis());
            ref.setValue(data);
        } catch (Exception e) {
            Log.w(TAG, "Realtime DB vitals sync bypassed: " + e.getMessage());
        }
    }

    public void syncRealtimeSosAlert(String uid, boolean isTriggered, String alertDetails) {
        if (!isAvailable || realtimeDb == null || uid == null) return;
        try {
            DatabaseReference ref = realtimeDb.getReference("users")
                    .child(uid)
                    .child("realtime_monitoring")
                    .child("emergency_sos");

            Map<String, Object> data = new HashMap<>();
            data.put("active_sos", isTriggered);
            data.put("details", alertDetails);
            data.put("timestamp", System.currentTimeMillis());
            ref.setValue(data);
        } catch (Exception e) {
            Log.w(TAG, "Realtime DB SOS sync bypassed: " + e.getMessage());
        }
    }

    public void listenToRealtimeVitals(String uid, VitalsListener listener) {
        if (!isAvailable || realtimeDb == null || uid == null) return;
        try {
            DatabaseReference ref = realtimeDb.getReference("users")
                    .child(uid)
                    .child("realtime_monitoring")
                    .child("baby_vitals");

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getValue() instanceof Map) {
                        //noinspection unchecked
                        listener.onVitalsReceived((Map<String, Object>) snapshot.getValue());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "Realtime DB listener cancelled: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "Failed attaching realtime listener: " + e.getMessage());
        }
    }

    // =========================================================================
    // Cloud Firestore Document Backups (Prescriptions, Vaccines, Baby Care)
    // =========================================================================

    public void backupMedicineToFirestore(String uid, MedicineEntity med) {
        if (!isAvailable || firestore == null || uid == null || med == null) return;
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("medicine_name", med.getName());
            map.put("dosage", med.getDosage());
            map.put("scheduled_time", med.getReminderTime());
            map.put("start_date_millis", med.getStartDateMillis());
            map.put("duration_days", med.getDurationDays());
            map.put("end_date_millis", med.getEndDateMillis());
            map.put("course_completed", med.isCourseCompleted());
            map.put("synced_at", System.currentTimeMillis());

            firestore.collection("maacare_users")
                    .document(uid)
                    .collection("prescriptions")
                    .document(String.valueOf(med.getId()))
                    .set(map, SetOptions.merge());
        } catch (Exception e) {
            Log.w(TAG, "Firestore medicine backup bypassed: " + e.getMessage());
        }
    }

    public void backupBabyEventToFirestore(String uid, BabyCareTimelineEntity event) {
        if (!isAvailable || firestore == null || uid == null || event == null) return;
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("activity_type", event.getActivityType());
            map.put("title", event.getTitle());
            map.put("details", event.getDetails());
            map.put("time_formatted", event.getTimeFormatted());
            map.put("timestamp_millis", event.getTimestampMillis());
            map.put("category_color", event.getCategoryColor());

            firestore.collection("maacare_users")
                    .document(uid)
                    .collection("baby_timeline")
                    .document(String.valueOf(event.getId()))
                    .set(map, SetOptions.merge());
        } catch (Exception e) {
            Log.w(TAG, "Firestore baby event backup bypassed: " + e.getMessage());
        }
    }

    public void backupVaccineToFirestore(String uid, VaccinationEntity vaccine) {
        if (!isAvailable || firestore == null || uid == null || vaccine == null) return;
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("vaccine_name", vaccine.getVaccineName());
            map.put("target_age", vaccine.getTargetAge());
            map.put("is_done", vaccine.isDone());
            map.put("disease_target", vaccine.getDiseaseTarget());
            map.put("target_age_days", vaccine.getTargetAgeDays());

            firestore.collection("maacare_users")
                    .document(uid)
                    .collection("vaccinations")
                    .document(String.valueOf(vaccine.getId()))
                    .set(map, SetOptions.merge());
        } catch (Exception e) {
            Log.w(TAG, "Firestore vaccine backup bypassed: " + e.getMessage());
        }
    }

    public void backupPhotoMemoryMetadata(String uid, String photoId, String caption,
                                          String localPath, @Nullable String cloudUrl, long timestamp) {
        if (!isAvailable || firestore == null || uid == null) return;
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("photo_id", photoId);
            map.put("caption", caption);
            map.put("local_path", localPath);
            map.put("cloud_url", cloudUrl != null ? cloudUrl : "");
            map.put("created_at", timestamp);

            firestore.collection("maacare_users")
                    .document(uid)
                    .collection("photo_memories")
                    .document(photoId)
                    .set(map, SetOptions.merge());
        } catch (Exception e) {
            Log.w(TAG, "Firestore photo memory backup bypassed: " + e.getMessage());
        }
    }

    // =========================================================================
    // Firebase Cloud Storage (Ultrasound & Baby Milestone Photos)
    // =========================================================================

    public void uploadMilestonePhoto(String uid, String photoId, File localFile, UploadCallback callback) {
        if (!isAvailable || storage == null || localFile == null || !localFile.exists()) {
            if (callback != null) {
                callback.onFailure(new IllegalStateException("Firebase Storage is unavailable or file not found."));
            }
            return;
        }

        try {
            StorageReference photoRef = storage.getReference()
                    .child("milestone_photos")
                    .child(uid != null ? uid : "demo_user")
                    .child(photoId + ".jpg");

            UploadTask uploadTask = photoRef.putFile(Uri.fromFile(localFile));
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    if (callback != null) {
                        callback.onSuccess(uri.toString());
                    }
                }).addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
            }).addOnFailureListener(e -> {
                if (callback != null) {
                    callback.onFailure(e);
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "Storage upload exception: " + e.getMessage());
            if (callback != null) {
                callback.onFailure(e);
            }
        }
    }
}
