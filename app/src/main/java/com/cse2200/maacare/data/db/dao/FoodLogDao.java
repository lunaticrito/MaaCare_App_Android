package com.cse2200.maacare.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.cse2200.maacare.data.db.entity.FoodLogEntity;
import java.util.List;

/**
 * Data Access Object for Maternal Food Log.
 */
@Dao
public interface FoodLogDao {

    @Query("SELECT * FROM food_logs ORDER BY logged_time_millis DESC")
    List<FoodLogEntity> getAllFoodLogs();

    @Insert
    long insert(FoodLogEntity log);

    @Delete
    int delete(FoodLogEntity log);

    @Query("DELETE FROM food_logs WHERE id = :id")
    int deleteById(long id);
}
