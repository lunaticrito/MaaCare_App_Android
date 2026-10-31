package com.cse2200.maacare.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model representing weekly fetal growth milestones and clinical guidance.
 */
public class FetalMilestone {
    @SerializedName("week")
    private int week;

    @SerializedName("trimester")
    private int trimester;

    @SerializedName("baby_size")
    private String babySize;

    @SerializedName("weight_grams")
    private double weightGrams;

    @SerializedName("length_cm")
    private double lengthCm;

    @SerializedName("development_summary")
    private String developmentSummary;

    @SerializedName("maternal_changes")
    private String maternalChanges;

    @SerializedName("clinical_advice")
    private String clinicalAdvice;

    public FetalMilestone() {
    }

    public FetalMilestone(int week, int trimester, String babySize, double weightGrams, double lengthCm,
                          String developmentSummary, String maternalChanges, String clinicalAdvice) {
        this.week = week;
        this.trimester = trimester;
        this.babySize = babySize;
        this.weightGrams = weightGrams;
        this.lengthCm = lengthCm;
        this.developmentSummary = developmentSummary;
        this.maternalChanges = maternalChanges;
        this.clinicalAdvice = clinicalAdvice;
    }

    public int getWeek() {
        return week;
    }

    public int getTrimester() {
        return trimester;
    }

    public String getBabySize() {
        return babySize;
    }

    public double getWeightGrams() {
        return weightGrams;
    }

    public double getLengthCm() {
        return lengthCm;
    }

    public String getDevelopmentSummary() {
        return developmentSummary;
    }

    public String getMaternalChanges() {
        return maternalChanges;
    }

    public String getClinicalAdvice() {
        return clinicalAdvice;
    }
}
