package com.cse2200.maacare.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing a prescribed medication or supplement reminder.
 */
@Entity(tableName = "medicines")
public class MedicineEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "dosage")
    private String dosage;

    @ColumnInfo(name = "reminder_time")
    private String reminderTime;

    @ColumnInfo(name = "is_taken_today")
    private boolean isTakenToday;

    @ColumnInfo(name = "reminder_enabled")
    private boolean reminderEnabled;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public MedicineEntity() {
    }

    public MedicineEntity(String name, String dosage, String reminderTime, boolean isTakenToday, boolean reminderEnabled) {
        this.name = name;
        this.dosage = dosage;
        this.reminderTime = reminderTime;
        this.isTakenToday = isTakenToday;
        this.reminderEnabled = reminderEnabled;
        this.createdAt = System.currentTimeMillis();
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

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isTakenToday() {
        return isTakenToday;
    }

    public void setTakenToday(boolean takenToday) {
        isTakenToday = takenToday;
    }

    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
