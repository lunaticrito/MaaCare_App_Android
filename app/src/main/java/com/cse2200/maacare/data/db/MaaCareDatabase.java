package com.cse2200.maacare.data.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.cse2200.maacare.data.db.dao.BabyCareDao;
import com.cse2200.maacare.data.db.dao.BabyEssentialsDao;
import com.cse2200.maacare.data.db.dao.BabyFoodLogDao;
import com.cse2200.maacare.data.db.dao.BabyGrowthDao;
import com.cse2200.maacare.data.db.dao.BabyMilestoneDao;
import com.cse2200.maacare.data.db.dao.BabyVitalsDao;
import com.cse2200.maacare.data.db.dao.DoctorDao;
import com.cse2200.maacare.data.db.dao.FoodLogDao;
import com.cse2200.maacare.data.db.dao.HydrationDao;
import com.cse2200.maacare.data.db.dao.MedicineDao;
import com.cse2200.maacare.data.db.dao.UserDao;
import com.cse2200.maacare.data.db.dao.VaccinationDao;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Room Database for MaaCare storing Users, Medicines, Vaccinations,
 * Hydration logs, Maternal Meals, and Infant Nutrition logs.
 */
@Database(
        entities = {
                UserEntity.class,
                MedicineEntity.class,
                VaccinationEntity.class,
                FoodLogEntity.class,
                HydrationLogEntity.class,
                BabyFoodLogEntity.class,
                BabyCareTimelineEntity.class,
                BabyMilestoneEntity.class,
                BabyGrowthEntity.class,
                BabyVitalsEntity.class,
                BabyEssentialItemEntity.class,
                DoctorEntity.class
        },
        version = 5,
        exportSchema = false
)
public abstract class MaaCareDatabase extends RoomDatabase {

    private static volatile MaaCareDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract UserDao userDao();
    public abstract MedicineDao medicineDao();
    public abstract VaccinationDao vaccinationDao();
    public abstract FoodLogDao foodLogDao();
    public abstract HydrationDao hydrationDao();
    public abstract BabyFoodLogDao babyFoodLogDao();
    public abstract BabyCareDao babyCareDao();
    public abstract BabyMilestoneDao babyMilestoneDao();
    public abstract BabyGrowthDao babyGrowthDao();
    public abstract BabyVitalsDao babyVitalsDao();
    public abstract BabyEssentialsDao babyEssentialsDao();
    public abstract DoctorDao doctorDao();

    public static MaaCareDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (MaaCareDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            MaaCareDatabase.class,
                            "maacare_database.db"
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            databaseWriteExecutor.execute(() -> {
                                DatabaseInitializer.populateInitialData(context.getApplicationContext(), INSTANCE);
                            });
                        }
                    }).build();
                }
            }
        }
        return INSTANCE;
    }
}
