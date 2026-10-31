package com.cse2200.maacare.data.repository;

import android.content.Context;
import android.util.Log;
import com.cse2200.maacare.data.db.MaaCareDatabase;
import com.cse2200.maacare.data.db.entity.FoodLogEntity;
import com.cse2200.maacare.data.db.entity.MedicineEntity;
import com.cse2200.maacare.data.db.entity.VaccinationEntity;
import com.cse2200.maacare.data.model.MaternalHealthDataResponse;
import com.cse2200.maacare.network.ApiClient;
import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Single source of truth with Graceful Offline Fallback repository architecture.
 */
public class MaaCareRepository {

    private static final String TAG = "MaaCareRepository";
    private final Context context;
    private final MaaCareDatabase database;

    public interface DataCallback<T> {
        void onSuccess(T result);
        void onError(Throwable error);
    }

    public MaaCareRepository(Context context) {
        this.context = context.getApplicationContext();
        this.database = MaaCareDatabase.getInstance(this.context);
    }

    /**
     * Fetches maternal health directory, milestones, and danger signs from network;
     * if network is unavailable or fails, gracefully falls back to local assets JSON.
     */
    public void getMaternalHealthData(DataCallback<MaternalHealthDataResponse> callback) {
        ApiClient.getApiService().getMaternalHealthData().enqueue(new Callback<MaternalHealthDataResponse>() {
            @Override
            public void onResponse(Call<MaternalHealthDataResponse> call, Response<MaternalHealthDataResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully fetched maternal health data from remote API.");
                    callback.onSuccess(response.body());
                } else {
                    Log.w(TAG, "Remote API returned code: " + response.code() + ", falling back to offline assets.");
                    loadOfflineFallbackData(callback);
                }
            }

            @Override
            public void onFailure(Call<MaternalHealthDataResponse> call, Throwable t) {
                Log.w(TAG, "Network request failed: " + t.getMessage() + ". Activating graceful local fallback.");
                loadOfflineFallbackData(callback);
            }
        });
    }

    /**
     * Reads and parses local JSON asset bundled with the APK.
     */
    public void loadOfflineFallbackData(DataCallback<MaternalHealthDataResponse> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            try {
                InputStream inputStream = context.getAssets().open("maternal_health_data.json");
                InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                MaternalHealthDataResponse data = new Gson().fromJson(reader, MaternalHealthDataResponse.class);
                reader.close();
                inputStream.close();
                callback.onSuccess(data);
            } catch (Exception e) {
                Log.e(TAG, "Failed to load offline assets fallback", e);
                callback.onError(e);
            }
        });
    }

    // Database access helpers
    public void getAllMedicines(DataCallback<List<MedicineEntity>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<MedicineEntity> list = database.medicineDao().getAllMedicines();
            callback.onSuccess(list);
        });
    }

    public void insertMedicine(MedicineEntity medicine, DataCallback<Long> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            long id = database.medicineDao().insert(medicine);
            callback.onSuccess(id);
        });
    }

    public void updateMedicine(MedicineEntity medicine, DataCallback<Integer> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            int rows = database.medicineDao().update(medicine);
            callback.onSuccess(rows);
        });
    }

    public void deleteMedicine(MedicineEntity medicine, DataCallback<Integer> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            int rows = database.medicineDao().delete(medicine);
            callback.onSuccess(rows);
        });
    }

    public void getAllVaccines(DataCallback<List<VaccinationEntity>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<VaccinationEntity> list = database.vaccinationDao().getAllVaccines();
            callback.onSuccess(list);
        });
    }

    public void updateVaccine(VaccinationEntity vaccine, DataCallback<Integer> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            int rows = database.vaccinationDao().update(vaccine);
            callback.onSuccess(rows);
        });
    }

    public void getAllFoodLogs(DataCallback<List<FoodLogEntity>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<FoodLogEntity> list = database.foodLogDao().getAllFoodLogs();
            callback.onSuccess(list);
        });
    }

    public void insertFoodLog(FoodLogEntity log, DataCallback<Long> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            long id = database.foodLogDao().insert(log);
            callback.onSuccess(id);
        });
    }

    public void deleteFoodLog(FoodLogEntity log, DataCallback<Integer> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            int rows = database.foodLogDao().delete(log);
            callback.onSuccess(rows);
        });
    }
}
