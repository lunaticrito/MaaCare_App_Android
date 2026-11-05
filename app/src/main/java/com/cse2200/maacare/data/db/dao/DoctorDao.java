package com.cse2200.maacare.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.cse2200.maacare.data.db.entity.DoctorEntity;
import java.util.List;

@Dao
public interface DoctorDao {

    @Query("SELECT * FROM doctors ORDER BY distanceKm ASC")
    List<DoctorEntity> getAllDoctors();

    @Query("SELECT * FROM doctors WHERE specialty LIKE '%' || :specialty || '%' ORDER BY distanceKm ASC")
    List<DoctorEntity> getDoctorsBySpecialty(String specialty);

    @Query("SELECT * FROM doctors WHERE name LIKE '%' || :query || '%' OR hospital LIKE '%' || :query || '%' OR specialty LIKE '%' || :query || '%' ORDER BY distanceKm ASC")
    List<DoctorEntity> searchDoctors(String query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DoctorEntity> doctors);
}
