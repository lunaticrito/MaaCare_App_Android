package com.cse2200.maacare.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing infant and toddler feeding logs with age milestones and allergy tracking.
 */
@Entity(tableName = "baby_food_logs")
public class BabyFoodLogEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "age_category")
    private String ageCategory; // e.g. "0-6 Months (EBF)", "6-8 Months", "9-11 Months", "12-24 Months"

    @ColumnInfo(name = "food_name")
    private String foodName;

    @ColumnInfo(name = "feeding_details")
    private String feedingDetails; // e.g. "Breastfed 15 mins", "Mashed Papaya 2 tbsp", "Khichuri 1 small bowl"

    @ColumnInfo(name = "allergy_reaction")
    private String allergyReaction; // e.g. "None", "Mild Rash", "Bloating", "Digestive Upset"

    @ColumnInfo(name = "logged_time_millis")
    private long loggedTimeMillis;

    public BabyFoodLogEntity() {
    }

    public BabyFoodLogEntity(String ageCategory, String foodName, String feedingDetails,
                            String allergyReaction, long loggedTimeMillis) {
        this.ageCategory = ageCategory;
        this.foodName = foodName;
        this.feedingDetails = feedingDetails;
        this.allergyReaction = allergyReaction;
        this.loggedTimeMillis = loggedTimeMillis;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAgeCategory() {
        return ageCategory;
    }

    public void setAgeCategory(String ageCategory) {
        this.ageCategory = ageCategory;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFeedingDetails() {
        return feedingDetails;
    }

    public void setFeedingDetails(String feedingDetails) {
        this.feedingDetails = feedingDetails;
    }

    public String getAllergyReaction() {
        return allergyReaction;
    }

    public void setAllergyReaction(String allergyReaction) {
        this.allergyReaction = allergyReaction;
    }

    public long getLoggedTimeMillis() {
        return loggedTimeMillis;
    }

    public void setLoggedTimeMillis(long loggedTimeMillis) {
        this.loggedTimeMillis = loggedTimeMillis;
    }
}
