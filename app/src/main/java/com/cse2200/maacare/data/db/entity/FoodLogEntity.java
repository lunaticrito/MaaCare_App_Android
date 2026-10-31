package com.cse2200.maacare.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing a maternal meal / nutrition log entry.
 */
@Entity(tableName = "food_logs")
public class FoodLogEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "meal_name")
    private String mealName;

    @ColumnInfo(name = "calories")
    private int calories;

    @ColumnInfo(name = "protein_grams")
    private double proteinGrams;

    @ColumnInfo(name = "logged_time_millis")
    private long loggedTimeMillis;

    public FoodLogEntity() {
    }

    public FoodLogEntity(String mealName, int calories, double proteinGrams, long loggedTimeMillis) {
        this.mealName = mealName;
        this.calories = calories;
        this.proteinGrams = proteinGrams;
        this.loggedTimeMillis = loggedTimeMillis;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public double getProteinGrams() {
        return proteinGrams;
    }

    public void setProteinGrams(double proteinGrams) {
        this.proteinGrams = proteinGrams;
    }

    public long getLoggedTimeMillis() {
        return loggedTimeMillis;
    }

    public void setLoggedTimeMillis(long loggedTimeMillis) {
        this.loggedTimeMillis = loggedTimeMillis;
    }
}
