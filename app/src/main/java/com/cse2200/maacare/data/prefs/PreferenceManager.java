package com.cse2200.maacare.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Encapsulates maternal profile and runtime settings via SharedPreferences.
 */
public class PreferenceManager {

    private static final String PREF_NAME = "maacare_user_prefs";
    private static final String KEY_MOTHER_NAME = "key_mother_name";
    private static final String KEY_GESTATIONAL_WEEK = "key_gestational_week";
    private static final String KEY_BLOOD_GROUP = "key_blood_group";
    private static final String KEY_EMERGENCY_PHONE = "key_emergency_phone";
    private static final String KEY_WATER_GLASSES = "key_water_glasses";
    private static final String KEY_MONITOR_ACTIVE = "key_monitor_active";

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getMotherName() {
        return prefs.getString(KEY_MOTHER_NAME, "Ayesha Rahman");
    }

    public void setMotherName(String name) {
        prefs.edit().putString(KEY_MOTHER_NAME, name).apply();
    }

    public int getGestationalWeek() {
        return prefs.getInt(KEY_GESTATIONAL_WEEK, 20);
    }

    public void setGestationalWeek(int week) {
        prefs.edit().putInt(KEY_GESTATIONAL_WEEK, Math.max(1, Math.min(40, week))).apply();
    }

    public String getBloodGroup() {
        return prefs.getString(KEY_BLOOD_GROUP, "O+");
    }

    public void setBloodGroup(String bloodGroup) {
        prefs.edit().putString(KEY_BLOOD_GROUP, bloodGroup).apply();
    }

    public String getEmergencyPhone() {
        return prefs.getString(KEY_EMERGENCY_PHONE, "+8801711000000");
    }

    public void setEmergencyPhone(String phone) {
        prefs.edit().putString(KEY_EMERGENCY_PHONE, phone).apply();
    }

    public int getWaterGlasses() {
        return prefs.getInt(KEY_WATER_GLASSES, 6);
    }

    public void setWaterGlasses(int count) {
        prefs.edit().putInt(KEY_WATER_GLASSES, Math.max(0, count)).apply();
    }

    public void incrementWaterGlasses() {
        setWaterGlasses(getWaterGlasses() + 1);
    }

    public boolean isMonitorActive() {
        return prefs.getBoolean(KEY_MONITOR_ACTIVE, false);
    }

    public void setMonitorActive(boolean active) {
        prefs.edit().putBoolean(KEY_MONITOR_ACTIVE, active).apply();
    }

    /**
     * Calculates remaining days until Estimated Due Date (EDD) assuming 40 total gestational weeks.
     */
    public int getDaysRemainingToEdd() {
        int currentWeek = getGestationalWeek();
        int weeksLeft = Math.max(0, 40 - currentWeek);
        return weeksLeft * 7;
    }
}
