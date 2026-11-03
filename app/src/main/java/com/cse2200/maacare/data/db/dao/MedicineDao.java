package com.cse2200.maacare.data.db.dao;

import android.database.Cursor;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.cse2200.maacare.data.db.entity.MedicineEntity;
import java.util.List;

/**
 * Data Access Object for Medicine Reminders. Supports both direct ORM queries
 * and Cursor-backed operations for ContentProvider access.
 */
@Dao
public interface MedicineDao {

    @Query("SELECT * FROM medicines ORDER BY id DESC")
    List<MedicineEntity> getAllMedicines();

    @Query("SELECT * FROM medicines WHERE reminder_enabled = 1")
    List<MedicineEntity> getActiveReminders();

    @Query("SELECT * FROM medicines WHERE end_date_millis > :currentTime AND reminder_enabled = 1")
    List<MedicineEntity> getActiveUnexpiredReminders(long currentTime);

    @Query("SELECT * FROM medicines WHERE id = :id LIMIT 1")
    MedicineEntity getMedicineById(long id);

    @Insert
    long insert(MedicineEntity medicine);

    @Update
    int update(MedicineEntity medicine);

    @Delete
    int delete(MedicineEntity medicine);

    @Query("DELETE FROM medicines WHERE id = :id")
    int deleteById(long id);

    // Cursor methods for ContentProvider integration
    @Query("SELECT * FROM medicines")
    Cursor selectAllCursor();

    @Query("SELECT * FROM medicines WHERE id = :id")
    Cursor selectByIdCursor(long id);
}
