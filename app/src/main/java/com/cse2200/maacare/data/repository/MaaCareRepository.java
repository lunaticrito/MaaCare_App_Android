package com.cse2200.maacare.data.repository;

import android.content.Context;
import android.util.Log;
import com.cse2200.maacare.data.db.MaaCareDatabase;
import com.cse2200.maacare.data.db.entity.BabyCareTimelineEntity;
import com.cse2200.maacare.data.db.entity.BabyEssentialItemEntity;
import com.cse2200.maacare.data.db.entity.BabyFoodLogEntity;
import com.cse2200.maacare.data.db.entity.BabyGrowthEntity;
import com.cse2200.maacare.data.db.entity.BabyMilestoneEntity;
import com.cse2200.maacare.data.db.entity.BabyVitalsEntity;
import com.cse2200.maacare.data.db.entity.DoctorEntity;
import com.cse2200.maacare.data.db.entity.FoodLogEntity;
import com.cse2200.maacare.data.db.entity.HydrationLogEntity;
import com.cse2200.maacare.data.db.entity.MedicineEntity;
import com.cse2200.maacare.data.db.entity.UserEntity;
import com.cse2200.maacare.data.db.entity.VaccinationEntity;
import com.cse2200.maacare.data.model.DangerSign;
import com.cse2200.maacare.data.model.MaternalHealthDataResponse;
import com.cse2200.maacare.network.ApiClient;
import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
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

    public void updateVaccineAdministered(long id, boolean isDone, DataCallback<Integer> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            VaccinationEntity v = database.vaccinationDao().getVaccineById(id);
            if (v != null) {
                v.setDone(isDone);
                int rows = database.vaccinationDao().update(v);
                callback.onSuccess(rows);
            } else {
                callback.onSuccess(0);
            }
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

    // --- Baby Care Daily Timeline ---

    public void getAllBabyEvents(DataCallback<List<BabyCareTimelineEntity>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<BabyCareTimelineEntity> list = database.babyCareDao().getAllEvents();
            callback.onSuccess(list);
        });
    }

    public void getBabyEventsByDate(String date, DataCallback<List<BabyCareTimelineEntity>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<BabyCareTimelineEntity> list = database.babyCareDao().getEventsByDate(date);
            callback.onSuccess(list);
        });
    }

    public void insertBabyEvent(BabyCareTimelineEntity event, DataCallback<Long> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            long id = database.babyCareDao().insert(event);
            callback.onSuccess(id);
        });
    }

    public void updateBabyEvent(BabyCareTimelineEntity event, DataCallback<Integer> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            int rows = database.babyCareDao().update(event);
            callback.onSuccess(rows);
        });
    }

    public void deleteBabyEvent(BabyCareTimelineEntity event, DataCallback<Integer> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            int rows = database.babyCareDao().delete(event);
            callback.onSuccess(rows);
        });
    }

    // --- Developmental Milestones ---

    public void getAllMilestones(DataCallback<List<BabyMilestoneEntity>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<BabyMilestoneEntity> list = database.babyMilestoneDao().getAllMilestones();
            callback.onSuccess(list);
        });
    }

    public void updateMilestone(long id, boolean isAchieved, String remarks, DataCallback<Integer> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            int rows = database.babyMilestoneDao().updateMilestoneStatus(id, isAchieved, remarks, System.currentTimeMillis());
            callback.onSuccess(rows);
        });
    }

    // --- Baby Growth & Anthropometrics ---

    public void getAllGrowthRecords(DataCallback<List<BabyGrowthEntity>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<BabyGrowthEntity> list = database.babyGrowthDao().getAllGrowthRecords();
            callback.onSuccess(list);
        });
    }

    public void insertGrowthRecord(BabyGrowthEntity growth, DataCallback<Long> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            long id = database.babyGrowthDao().insert(growth);
            callback.onSuccess(id);
        });
    }

    public void upsertGrowthRecord(int ageMonths, double heightCm, double weightKg, DataCallback<BabyGrowthEntity> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            BabyGrowthEntity existing = database.babyGrowthDao().getRecordByAgeMonth(ageMonths);
            long now = System.currentTimeMillis();
            if (existing != null) {
                existing.setHeightCm(heightCm);
                existing.setWeightKg(weightKg);
                existing.setDateRecordedMillis(now);
                existing.setAtBirth(ageMonths == 0);
                database.babyGrowthDao().update(existing);
                callback.onSuccess(existing);
            } else {
                BabyGrowthEntity created = new BabyGrowthEntity(ageMonths, heightCm, weightKg, now, ageMonths == 0);
                long id = database.babyGrowthDao().insert(created);
                created.setId(id);
                callback.onSuccess(created);
            }
        });
    }

    public void deleteGrowthRecord(BabyGrowthEntity growth, DataCallback<Integer> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            int rows = database.babyGrowthDao().delete(growth);
            callback.onSuccess(rows);
        });
    }

    // --- Baby Vitals & Health ---

    public void getAllVitals(DataCallback<List<BabyVitalsEntity>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<BabyVitalsEntity> list = database.babyVitalsDao().getAllVitals();
            callback.onSuccess(list);
        });
    }

    public void getLatestVitals(DataCallback<BabyVitalsEntity> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            BabyVitalsEntity vitals = database.babyVitalsDao().getLatestVitals();
            callback.onSuccess(vitals);
        });
    }

    public void insertVitals(BabyVitalsEntity vitals, DataCallback<Long> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            long id = database.babyVitalsDao().insert(vitals);
            callback.onSuccess(id);
        });
    }

    public void updateVitals(BabyVitalsEntity vitals, DataCallback<Integer> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            int rows = database.babyVitalsDao().update(vitals);
            callback.onSuccess(rows);
        });
    }

    public void deleteVitals(BabyVitalsEntity vitals, DataCallback<Integer> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            int rows = database.babyVitalsDao().delete(vitals);
            callback.onSuccess(rows);
        });
    }

    // --- Hospital Bag & Essentials Checklist ---

    public void getAllEssentials(DataCallback<List<BabyEssentialItemEntity>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<BabyEssentialItemEntity> list = database.babyEssentialsDao().getAllItems();
            callback.onSuccess(list);
        });
    }

    public void getEssentialsByCategory(String category, DataCallback<List<BabyEssentialItemEntity>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<BabyEssentialItemEntity> list = database.babyEssentialsDao().getItemsByCategory(category);
            callback.onSuccess(list);
        });
    }

    public void updateEssentialChecked(long id, boolean isChecked, DataCallback<Integer> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            int rows = database.babyEssentialsDao().updateItemChecked(id, isChecked);
            callback.onSuccess(rows);
        });
    }

    // --- Emergency XML Protocol Parsing (XmlPullParser) ---

    public void getEmergencyProtocolsFromXml(DataCallback<List<DangerSign>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<DangerSign> protocols = new ArrayList<>();
            try (InputStream is = context.getAssets().open("emergency_health_protocols.xml")) {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(is, "UTF-8");

                String signName = "", signNameBn = "", riskLevel = "", desc = "", descBn = "", action = "", actionBn = "";
                String currentTag = null;
                int eventType = parser.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        currentTag = parser.getName();
                        if ("danger_sign".equals(currentTag)) {
                            signName = ""; signNameBn = ""; riskLevel = "";
                            desc = ""; descBn = ""; action = ""; actionBn = "";
                        }
                    } else if (eventType == XmlPullParser.TEXT && currentTag != null) {
                        String text = parser.getText().trim();
                        if (!text.isEmpty()) {
                            switch (currentTag) {
                                case "sign_name": signName = text; break;
                                case "sign_name_bn": signNameBn = text; break;
                                case "risk_level": riskLevel = text; break;
                                case "description": desc = text; break;
                                case "description_bn": descBn = text; break;
                                case "first_aid_action": action = text; break;
                                case "first_aid_action_bn": actionBn = text; break;
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if ("danger_sign".equals(parser.getName())) {
                            protocols.add(new DangerSign(signName, signNameBn, riskLevel, desc, descBn, action, actionBn));
                        }
                        currentTag = null;
                    }
                    eventType = parser.next();
                }
                callback.onSuccess(protocols);
            } catch (Exception e) {
                Log.e(TAG, "Failed parsing emergency_health_protocols.xml", e);
                callback.onError(e);
            }
        });
    }

    // --- Doctor Directory (8 Divisions & 64 Districts) ---

    public void getAllDoctors(DataCallback<List<DoctorEntity>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<DoctorEntity> list = database.doctorDao().getAllDoctors();
            callback.onSuccess(list);
        });
    }

    public void getDistrictsByDivision(String division, DataCallback<List<String>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<String> list = database.doctorDao().getDistrictsByDivision(division != null ? division : "");
            callback.onSuccess(list);
        });
    }

    public void filterDoctors(String division, String district, String specialtyCategory, String query,
                              DataCallback<List<DoctorEntity>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<DoctorEntity> list = database.doctorDao().filterDoctors(
                    division != null ? division : "",
                    district != null ? district : "",
                    specialtyCategory != null ? specialtyCategory : "",
                    query != null ? query : ""
            );
            callback.onSuccess(list);
        });
    }

    public void searchDoctors(String query, DataCallback<List<DoctorEntity>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<DoctorEntity> list = database.doctorDao().searchDoctors(query);
            callback.onSuccess(list);
        });
    }

    public void getDoctorsBySpecialty(String specialty, DataCallback<List<DoctorEntity>> callback) {
        MaaCareDatabase.databaseWriteExecutor.execute(() -> {
            List<DoctorEntity> list = database.doctorDao().getDoctorsBySpecialty(specialty);
            callback.onSuccess(list);
        });
    }
}
