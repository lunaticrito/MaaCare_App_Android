package com.cse2200.maacare.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Bilingual model representing weekly fetal growth milestones and clinical guidance.
 */
public class FetalMilestone {
    @SerializedName("week")
    private int week;

    @SerializedName("trimester")
    private int trimester;

    @SerializedName("baby_size")
    private String babySize;

    @SerializedName("baby_size_bn")
    private String babySizeBn;

    @SerializedName("weight_grams")
    private double weightGrams;

    @SerializedName("length_cm")
    private double lengthCm;

    @SerializedName("development_summary")
    private String developmentSummary;

    @SerializedName("development_summary_bn")
    private String developmentSummaryBn;

    @SerializedName("maternal_changes")
    private String maternalChanges;

    @SerializedName("maternal_changes_bn")
    private String maternalChangesBn;

    @SerializedName("clinical_advice")
    private String clinicalAdvice;

    @SerializedName("clinical_advice_bn")
    private String clinicalAdviceBn;

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

    public int getWeek() { return week; }
    public int getTrimester() { return trimester; }
    public String getBabySize() { return babySize; }
    public String getBabySizeBn() { return babySizeBn; }
    public double getWeightGrams() { return weightGrams; }
    public double getLengthCm() { return lengthCm; }
    public String getDevelopmentSummary() { return developmentSummary; }
    public String getDevelopmentSummaryBn() { return developmentSummaryBn; }
    public String getMaternalChanges() { return maternalChanges; }
    public String getMaternalChangesBn() { return maternalChangesBn; }
    public String getClinicalAdvice() { return clinicalAdvice; }
    public String getClinicalAdviceBn() { return clinicalAdviceBn; }
}
