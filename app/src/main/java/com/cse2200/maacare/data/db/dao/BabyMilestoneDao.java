package com.cse2200.maacare.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.cse2200.maacare.data.db.entity.BabyMilestoneEntity;
import java.util.List;

@Dao
public interface BabyMilestoneDao {

    @Query("SELECT * FROM baby_milestones ORDER BY id ASC")
    List<BabyMilestoneEntity> getAllMilestones();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BabyMilestoneEntity milestone);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BabyMilestoneEntity> milestones);

    @Update
    int update(BabyMilestoneEntity milestone);

    @Query("UPDATE baby_milestones SET isAchieved = :achieved, remarkText = :remarks, lastUpdatedMillis = :timestamp WHERE id = :id")
    int updateMilestoneStatus(long id, boolean achieved, String remarks, long timestamp);
}
