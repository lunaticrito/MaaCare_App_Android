package com.cse2200.maacare.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing a prescribed medication or supplement reminder with course duration tracking.
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

    @ColumnInfo(name = "start_date_millis")
    private long startDateMillis;

    @ColumnInfo(name = "duration_days")
    private int durationDays;

    @ColumnInfo(name = "end_date_millis")
    private long endDateMillis;

    @ColumnInfo(name = "is_taken_today")
    private boolean isTakenToday;

    @ColumnInfo(name = "reminder_enabled")
    private boolean reminderEnabled;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public MedicineEntity() {
    }

    public MedicineEntity(String name, String dosage, String reminderTime, long startDateMillis,
                          int durationDays, boolean isTakenToday, boolean reminderEnabled) {
        this.name = name;
        this.dosage = dosage;
        this.reminderTime = reminderTime;
        this.startDateMillis = startDateMillis;
        this.durationDays = durationDays;
        this.endDateMillis = startDateMillis + (durationDays * 86400000L);
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

    public long getStartDateMillis() {
        return startDateMillis;
    }

    public void setStartDateMillis(long startDateMillis) {
        this.startDateMillis = startDateMillis;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public long getEndDateMillis() {
        return endDateMillis;
    }

    public void setEndDateMillis(long endDateMillis) {
        this.endDateMillis = endDateMillis;
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

    // --- Course Tracking Helpers ---

    public boolean isCourseCompleted() {
        return System.currentTimeMillis() > endDateMillis;
    }

    public int getCurrentDayInCourse() {
        long elapsed = System.currentTimeMillis() - startDateMillis;
        if (elapsed < 0) return 1;
        int day = (int) (elapsed / 86400000L) + 1;
        return Math.min(day, durationDays);
    }

    public int getDaysRemainingInCourse() {
        long remaining = endDateMillis - System.currentTimeMillis();
        if (remaining <= 0) return 0;
        return (int) Math.ceil(remaining / 86400000.0);
    }

    public int getCourseProgressPercent() {
        if (durationDays <= 0) return 100;
        int currentDay = getCurrentDayInCourse();
        return Math.min(100, Math.max(0, (int) (((double) currentDay / durationDays) * 100)));
    }
}
