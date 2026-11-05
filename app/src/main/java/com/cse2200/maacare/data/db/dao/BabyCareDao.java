package com.cse2200.maacare.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.cse2200.maacare.data.db.entity.BabyCareTimelineEntity;
import java.util.List;

@Dao
public interface BabyCareDao {

    @Query("SELECT * FROM baby_timeline ORDER BY timestampMillis DESC")
    List<BabyCareTimelineEntity> getAllEvents();

    @Query("SELECT * FROM baby_timeline WHERE dateString = :date ORDER BY timestampMillis DESC")
    List<BabyCareTimelineEntity> getEventsByDate(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BabyCareTimelineEntity event);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BabyCareTimelineEntity> events);

    @Update
    int update(BabyCareTimelineEntity event);

    @Delete
    int delete(BabyCareTimelineEntity event);

    @Query("DELETE FROM baby_timeline")
    void deleteAll();
}
