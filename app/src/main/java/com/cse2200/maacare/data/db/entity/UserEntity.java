package com.cse2200.maacare.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing an authenticated mother or caregiver account.
 */
@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "email_or_phone")
    private String emailOrPhone;

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "lmp_date_millis")
    private long lmpDateMillis;

    @ColumnInfo(name = "baby_birth_millis")
    private long babyBirthMillis;

    @ColumnInfo(name = "blood_group")
    private String bloodGroup;

    @ColumnInfo(name = "emergency_phone")
    private String emergencyPhone;

    public UserEntity() {
    }

    public UserEntity(String name, String emailOrPhone, String password, long lmpDateMillis,
                      long babyBirthMillis, String bloodGroup, String emergencyPhone) {
        this.name = name;
        this.emailOrPhone = emailOrPhone;
        this.password = password;
        this.lmpDateMillis = lmpDateMillis;
        this.babyBirthMillis = babyBirthMillis;
        this.bloodGroup = bloodGroup;
        this.emergencyPhone = emergencyPhone;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailOrPhone() {
        return emailOrPhone;
    }

    public void setEmailOrPhone(String emailOrPhone) {
        this.emailOrPhone = emailOrPhone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getLmpDateMillis() {
        return lmpDateMillis;
    }

    public void setLmpDateMillis(long lmpDateMillis) {
        this.lmpDateMillis = lmpDateMillis;
    }

    public long getBabyBirthMillis() {
        return babyBirthMillis;
    }

    public void setBabyBirthMillis(long babyBirthMillis) {
        this.babyBirthMillis = babyBirthMillis;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }
}
