package com.cse2200.maacare.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.cse2200.maacare.data.db.entity.BabyVitalsEntity;
import java.util.List;

@Dao
public interface BabyVitalsDao {

    @Query("SELECT * FROM baby_vitals ORDER BY recordedMillis DESC")
    List<BabyVitalsEntity> getAllVitals();

    @Query("SELECT * FROM baby_vitals ORDER BY recordedMillis DESC LIMIT 1")
    BabyVitalsEntity getLatestVitals();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BabyVitalsEntity vitals);
}
