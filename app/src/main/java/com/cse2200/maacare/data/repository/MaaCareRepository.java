package com.cse2200.maacare.data.repository;

import android.content.Context;
import android.util.Log;
import com.cse2200.maacare.data.db.MaaCareDatabase;
import com.cse2200.maacare.data.db.entity.BabyFoodLogEntity;
import com.cse2200.maacare.data.db.entity.FoodLogEntity;
import com.cse2200.maacare.data.db.entity.HydrationLogEntity;
import com.cse2200.maacare.data.db.entity.MedicineEntity;
import com.cse2200.maacare.data.db.entity.UserEntity;
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
 * Single source of truth repository managing User Auth, Medicines with Course Tracking,
 * WHO/EPI Vaccines, Hydration Logs, Baby Diet Logs, and Remote API / Offline JSON data.
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

    // --- Authentication ---

    public void login(String identifier, String password, DataCallback<UserEntity> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            UserEntity user = database.userDao().login(identifier, password);
            callback.onSuccess(user);
        });
    }

    public void registerUser(UserEntity user, DataCallback<Long> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            long id = database.userDao().registerUser(user);
            callback.onSuccess(id);
        });
    }

    public void updateUser(UserEntity user, DataCallback<Integer> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            int rows = database.userDao().updateUser(user);
            callback.onSuccess(rows);
        });
    }

    // --- Medicines with Course Durations ---

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

    // --- Vaccinations (EPI & WHO Milestones) ---

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

    // --- Maternal Nutrition & Meal Logs ---

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

    // --- Baby Food & Nutrition Guide Logs ---

    public void getAllBabyFoodLogs(DataCallback<List<BabyFoodLogEntity>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<BabyFoodLogEntity> list = database.babyFoodLogDao().getAllBabyFoodLogs();
            callback.onSuccess(list);
        });
    }

    public void insertBabyFoodLog(BabyFoodLogEntity log, DataCallback<Long> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            long id = database.babyFoodLogDao().insert(log);
            callback.onSuccess(id);
        });
    }

    public void deleteBabyFoodLog(BabyFoodLogEntity log, DataCallback<Integer> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            int rows = database.babyFoodLogDao().delete(log);
            callback.onSuccess(rows);
        });
    }

    // --- Hydration Logging ---

    public void getTodayHydrationTotal(String dateString, DataCallback<Integer> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            Integer total = database.hydrationDao().getDailyTotalVolumeMl(dateString);
            callback.onSuccess(total != null ? total : 0);
        });
    }

    public void insertHydrationLog(HydrationLogEntity log, DataCallback<Long> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            long id = database.hydrationDao().insert(log);
            callback.onSuccess(id);
        });
    }

    public void clearTodayHydration(String dateString, DataCallback<Integer> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            int rows = database.hydrationDao().clearLogsForDate(dateString);
            callback.onSuccess(rows);
        });
    }

    // --- Remote API with Graceful Offline Fallback ---

    public void getMaternalHealthData(DataCallback<MaternalHealthDataResponse> callback) {
        ApiClient.getApiService().getMaternalHealthData().enqueue(new Callback<MaternalHealthDataResponse>() {
            @Override
            public void onResponse(Call<MaternalHealthDataResponse> call, Response<MaternalHealthDataResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Fetched data from remote API successfully.");
                    callback.onSuccess(response.body());
                } else {
                    Log.w(TAG, "Remote API error, falling back to local asset JSON.");
                    loadOfflineFallbackData(callback);
                }
            }

            @Override
            public void onFailure(Call<MaternalHealthDataResponse> call, Throwable t) {
                Log.w(TAG, "Network unavailable, activating offline JSON fallback.");
                loadOfflineFallbackData(callback);
            }
        });
    }

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
                Log.e(TAG, "Failed reading bundled offline JSON", e);
                callback.onError(e);
            }
        });
    }
}
