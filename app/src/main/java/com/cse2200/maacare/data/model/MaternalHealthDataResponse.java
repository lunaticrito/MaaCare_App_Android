package com.cse2200.maacare.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulating response object for maternal health tips, milestones, and directory.
 */
public class MaternalHealthDataResponse {
    @SerializedName("fetal_milestones")
    private List<FetalMilestone> fetalMilestones = new ArrayList<>();

    @SerializedName("nutrition_advisories")
    private List<NutritionAdvisory> nutritionAdvisories = new ArrayList<>();

    @SerializedName("emergency_directory")
    private List<HospitalContact> emergencyDirectory = new ArrayList<>();

    @SerializedName("danger_signs")
    private List<DangerSign> dangerSigns = new ArrayList<>();

    public List<FetalMilestone> getFetalMilestones() {
        return fetalMilestones;
    }

    public List<NutritionAdvisory> getNutritionAdvisories() {
        return nutritionAdvisories;
    }

    public List<HospitalContact> getEmergencyDirectory() {
        return emergencyDirectory;
    }

    public List<DangerSign> getDangerSigns() {
        return dangerSigns;
    }
}
