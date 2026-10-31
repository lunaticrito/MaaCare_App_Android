package com.cse2200.maacare.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model representing maternity hospital contacts and emergency facilities.
 */
public class HospitalContact {
    @SerializedName("facility_name")
    private String facilityName;

    @SerializedName("category")
    private String category;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("address")
    private String address;

    public HospitalContact() {
    }

    public HospitalContact(String facilityName, String category, String phoneNumber, String address) {
        this.facilityName = facilityName;
        this.category = category;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public String getCategory() {
        return category;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }
}
