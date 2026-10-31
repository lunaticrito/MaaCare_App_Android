package com.cse2200.maacare.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model representing maternal dietary guidelines and nutrient advisories.
 */
public class NutritionAdvisory {
    @SerializedName("title")
    private String title;

    @SerializedName("category")
    private String category;

    @SerializedName("description")
    private String description;

    @SerializedName("dosage_guideline")
    private String dosageGuideline;

    public NutritionAdvisory() {
    }

    public NutritionAdvisory(String title, String category, String description, String dosageGuideline) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.dosageGuideline = dosageGuideline;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getDosageGuideline() {
        return dosageGuideline;
    }
}
