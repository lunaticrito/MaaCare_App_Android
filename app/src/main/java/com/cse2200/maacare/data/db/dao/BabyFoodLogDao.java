package com.cse2200.maacare.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.cse2200.maacare.data.db.entity.BabyFoodLogEntity;
import java.util.List;

/**
 * Data Access Object for Infant and Toddler Nutrition & Allergy tracking.
 */
@Dao
public interface BabyFoodLogDao {

    @Query("SELECT * FROM baby_food_logs ORDER BY logged_time_millis DESC")
    List<BabyFoodLogEntity> getAllBabyFoodLogs();

    @Query("SELECT * FROM baby_food_logs WHERE age_category = :category ORDER BY logged_time_millis DESC")
    List<BabyFoodLogEntity> getLogsByCategory(String category);

    @Insert
    long insert(BabyFoodLogEntity log);

    @Delete
    int delete(BabyFoodLogEntity log);
}
