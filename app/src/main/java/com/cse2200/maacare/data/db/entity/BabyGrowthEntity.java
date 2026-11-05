package com.cse2200.maacare.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Anthropometric record tracking infant age, height in cm, and weight in kg.
 */
@Entity(tableName = "baby_growth")
public class BabyGrowthEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private int ageMonths;
    private double heightCm;
    private double weightKg;
    private long dateRecordedMillis;
    private boolean isAtBirth;

    public BabyGrowthEntity(int ageMonths, double heightCm, double weightKg,
                            long dateRecordedMillis, boolean isAtBirth) {
        this.ageMonths = ageMonths;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.dateRecordedMillis = dateRecordedMillis;
        this.isAtBirth = isAtBirth;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getAgeMonths() { return ageMonths; }
    public void setAgeMonths(int ageMonths) { this.ageMonths = ageMonths; }

    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public long getDateRecordedMillis() { return dateRecordedMillis; }
    public void setDateRecordedMillis(long dateRecordedMillis) { this.dateRecordedMillis = dateRecordedMillis; }

    public boolean isAtBirth() { return isAtBirth; }
    public void setAtBirth(boolean atBirth) { isAtBirth = atBirth; }
}
