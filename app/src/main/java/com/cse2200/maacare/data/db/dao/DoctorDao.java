package com.cse2200.maacare.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.cse2200.maacare.data.db.entity.DoctorEntity;
import java.util.List;

@Dao
public interface DoctorDao {

    @Query("SELECT * FROM doctors ORDER BY id ASC")
    List<DoctorEntity> getAllDoctors();

    @Query("SELECT DISTINCT district FROM doctors WHERE (:division = '' OR division = :division) ORDER BY district ASC")
    List<String> getDistrictsByDivision(String division);

    @Query("SELECT * FROM doctors WHERE "
            + "(:division = '' OR division = :division) "
            + "AND (:district = '' OR district = :district) "
            + "AND (:specialtyCategory = '' OR specialtyCategory = :specialtyCategory) "
            + "AND (:query = '' OR name LIKE '%' || :query || '%' OR hospital LIKE '%' || :query || '%' "
            + "OR district LIKE '%' || :query || '%' OR division LIKE '%' || :query || '%' OR specialty LIKE '%' || :query || '%') "
            + "ORDER BY id ASC")
    List<DoctorEntity> filterDoctors(String division, String district, String specialtyCategory, String query);

    @Query("SELECT * FROM doctors WHERE specialty LIKE '%' || :specialty || '%' ORDER BY distanceKm ASC")
    List<DoctorEntity> getDoctorsBySpecialty(String specialty);

    @Query("SELECT * FROM doctors WHERE name LIKE '%' || :query || '%' OR hospital LIKE '%' || :query || '%' OR specialty LIKE '%' || :query || '%' OR district LIKE '%' || :query || '%' ORDER BY distanceKm ASC")
    List<DoctorEntity> searchDoctors(String query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DoctorEntity> doctors);
}
