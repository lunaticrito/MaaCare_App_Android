package com.cse2200.maacare.data.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.cse2200.maacare.data.db.dao.FoodLogDao;
import com.cse2200.maacare.data.db.dao.MedicineDao;
import com.cse2200.maacare.data.db.dao.VaccinationDao;
import com.cse2200.maacare.data.db.entity.FoodLogEntity;
import com.cse2200.maacare.data.db.entity.MedicineEntity;
import com.cse2200.maacare.data.db.entity.VaccinationEntity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Room Database for MaaCare storing Medicines, Vaccinations, and Maternal Food Logs.
 */
@Database(entities = {MedicineEntity.class, VaccinationEntity.class, FoodLogEntity.class}, version = 1, exportSchema = false)
public abstract class MaaCareDatabase extends RoomDatabase {

    private static volatile MaaCareDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract MedicineDao medicineDao();
    public abstract VaccinationDao vaccinationDao();
    public abstract FoodLogDao foodLogDao();

    public static MaaCareDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (MaaCareDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            MaaCareDatabase.class,
                            "maacare_database.db"
                    ).addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            databaseWriteExecutor.execute(() -> {
                                DatabaseInitializer.populateInitialData(INSTANCE);
                            });
                        }
                    }).build();
                }
            }
        }
        return INSTANCE;
    }
}
