package com.cse2200.maacare.data.db.dao;

import android.database.Cursor;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.cse2200.maacare.data.db.entity.HydrationLogEntity;
import java.util.List;

/**
 * Data Access Object for Maternal Hydration logs and total daily volume queries.
 */
@Dao
public interface HydrationDao {

    @Query("SELECT SUM(volume_ml) FROM hydration_logs WHERE date_string = :dateString")
    Integer getDailyTotalVolumeMl(String dateString);

    @Query("SELECT * FROM hydration_logs WHERE date_string = :dateString ORDER BY logged_time_millis DESC")
    List<HydrationLogEntity> getLogsForDate(String dateString);

    @Insert
    long insert(HydrationLogEntity log);

    @Delete
    int delete(HydrationLogEntity log);

    @Query("DELETE FROM hydration_logs WHERE date_string = :dateString")
    int clearLogsForDate(String dateString);

    @Query("SELECT * FROM hydration_logs ORDER BY logged_time_millis DESC")
    Cursor selectAllCursor();

    @Query("SELECT * FROM hydration_logs WHERE id = :id LIMIT 1")
    Cursor selectByIdCursor(long id);
}
