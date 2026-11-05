package com.cse2200.maacare.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.cse2200.maacare.data.db.entity.BabyEssentialItemEntity;
import java.util.List;

@Dao
public interface BabyEssentialsDao {

    @Query("SELECT * FROM baby_essentials ORDER BY id ASC")
    List<BabyEssentialItemEntity> getAllItems();

    @Query("SELECT * FROM baby_essentials WHERE category = :category ORDER BY id ASC")
    List<BabyEssentialItemEntity> getItemsByCategory(String category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BabyEssentialItemEntity> items);

    @Update
    int update(BabyEssentialItemEntity item);

    @Query("UPDATE baby_essentials SET isChecked = :checked WHERE id = :id")
    int updateItemChecked(long id, boolean checked);
}
