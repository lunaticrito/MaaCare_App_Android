package com.cse2200.maacare.data.db.dao;

import android.database.Cursor;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.cse2200.maacare.data.db.entity.VaccinationEntity;
import java.util.List;

/**
 * Data Access Object for Newborn Vaccinations with EPI Schedule.
 */
@Dao
public interface VaccinationDao {

    @Query("SELECT * FROM vaccinations ORDER BY id ASC")
    List<VaccinationEntity> getAllVaccines();

    @Query("SELECT * FROM vaccinations WHERE id = :id LIMIT 1")
    VaccinationEntity getVaccineById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(VaccinationEntity vaccine);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<VaccinationEntity> vaccines);

    @Update
    int update(VaccinationEntity vaccine);

    @Delete
    int delete(VaccinationEntity vaccine);

    @Query("DELETE FROM vaccinations WHERE id = :id")
    int deleteById(long id);

    // Cursor methods for ContentProvider integration
    @Query("SELECT * FROM vaccinations")
    Cursor selectAllCursor();

    @Query("SELECT * FROM vaccinations WHERE id = :id")
    Cursor selectByIdCursor(long id);
}
