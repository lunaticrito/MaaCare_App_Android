package com.cse2200.maacare.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.cse2200.maacare.data.db.entity.BabyGrowthEntity;
import java.util.List;

@Dao
public interface BabyGrowthDao {

    @Query("SELECT * FROM baby_growth ORDER BY ageMonths ASC")
    List<BabyGrowthEntity> getAllGrowthRecords();

    @Query("SELECT * FROM baby_growth WHERE isAtBirth = 1 LIMIT 1")
    BabyGrowthEntity getBirthRecord();

    @Query("SELECT * FROM baby_growth WHERE ageMonths = :ageMonths LIMIT 1")
    BabyGrowthEntity getRecordByAgeMonth(int ageMonths);

    @Query("SELECT * FROM baby_growth ORDER BY ageMonths DESC LIMIT 1")
    BabyGrowthEntity getLatestRecord();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BabyGrowthEntity growth);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BabyGrowthEntity> records);

    @Update
    int update(BabyGrowthEntity growth);

    @Delete
    int delete(BabyGrowthEntity growth);
}
