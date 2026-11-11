package com.cse2200.maacare.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Bilingual model representing high-risk obstetric danger signs and emergency first-aid protocols.
 */
public class DangerSign {
    @SerializedName("sign_name")
    private String signName;

    @SerializedName("sign_name_bn")
    private String signNameBn;

    @SerializedName("risk_level")
    private String riskLevel;

    @SerializedName("description")
    private String description;

    @SerializedName("description_bn")
    private String descriptionBn;

    @SerializedName("first_aid_action")
    private String firstAidAction;

    @SerializedName("first_aid_action_bn")
    private String firstAidActionBn;

    public DangerSign() {
    }

    public DangerSign(String signName, String riskLevel, String description, String firstAidAction) {
        this.signName = signName;
        this.riskLevel = riskLevel;
        this.description = description;
        this.firstAidAction = firstAidAction;
    }

    public DangerSign(String signName, String signNameBn, String riskLevel,
                      String description, String descriptionBn,
                      String firstAidAction, String firstAidActionBn) {
        this.signName = signName;
        this.signNameBn = signNameBn;
        this.riskLevel = riskLevel;
        this.description = description;
        this.descriptionBn = descriptionBn;
        this.firstAidAction = firstAidAction;
        this.firstAidActionBn = firstAidActionBn;
    }

    public String getSignName() { return signName; }
    public String getSignNameBn() { return signNameBn; }
    public String getRiskLevel() { return riskLevel; }
    public String getDescription() { return description; }
    public String getDescriptionBn() { return descriptionBn; }
    public String getFirstAidAction() { return firstAidAction; }
    public String getFirstAidActionBn() { return firstAidActionBn; }
}
