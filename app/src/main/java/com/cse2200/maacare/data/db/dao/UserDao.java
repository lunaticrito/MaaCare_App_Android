package com.cse2200.maacare.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.cse2200.maacare.data.db.entity.UserEntity;

/**
 * Data Access Object for User Authentication & Profile Persistence.
 */
@Dao
public interface UserDao {

    @Query("SELECT * FROM users WHERE (email_or_phone = :identifier) AND password = :password LIMIT 1")
    UserEntity login(String identifier, String password);

    @Query("SELECT * FROM users WHERE email_or_phone = :identifier LIMIT 1")
    UserEntity getUserByEmailOrPhone(String identifier);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    UserEntity getUserById(long userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long registerUser(UserEntity user);

    @Update
    int updateUser(UserEntity user);
}
