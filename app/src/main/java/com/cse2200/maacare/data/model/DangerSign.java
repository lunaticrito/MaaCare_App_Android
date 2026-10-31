package com.cse2200.maacare.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model representing high-risk obstetric danger signs and emergency first-aid protocols.
 */
public class DangerSign {
    @SerializedName("sign_name")
    private String signName;

    @SerializedName("risk_level")
    private String riskLevel;

    @SerializedName("description")
    private String description;

    @SerializedName("first_aid_action")
    private String firstAidAction;

    public DangerSign() {
    }

    public DangerSign(String signName, String riskLevel, String description, String firstAidAction) {
        this.signName = signName;
        this.riskLevel = riskLevel;
        this.description = description;
        this.firstAidAction = firstAidAction;
    }

    public String getSignName() {
        return signName;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getDescription() {
        return description;
    }

    public String getFirstAidAction() {
        return firstAidAction;
    }
}
