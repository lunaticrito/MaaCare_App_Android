package com.cse2200.maacare.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Encapsulates user session, real-time deterministic gestational progress calculations,
 * infant chronological milestones, and hydration metrics.
 */
public class PreferenceManager {

    private static final String PREF_NAME = "maacare_user_prefs";

    private static final String KEY_IS_LOGGED_IN = "key_is_logged_in";
    private static final String KEY_USER_ID = "key_user_id";
    private static final String KEY_MOTHER_NAME = "key_mother_name";
    private static final String KEY_USER_EMAIL_PHONE = "key_user_email_phone";
    private static final String KEY_LMP_DATE_MILLIS = "key_lmp_date_millis";
    private static final String KEY_BABY_BIRTH_MILLIS = "key_baby_birth_millis";
    private static final String KEY_BLOOD_GROUP = "key_blood_group";
    private static final String KEY_EMERGENCY_PHONE = "key_emergency_phone";
    private static final String KEY_EMERGENCY_CONTACT_NAME = "key_emergency_contact_name";
    private static final String KEY_FIREBASE_UID = "key_firebase_uid";
    private static final String KEY_PHOTO_MEMORY_URI = "key_photo_memory_uri";

    private static final String KEY_WATER_GOAL_ML = "key_water_goal_ml";
    private static final String KEY_WATER_INTAKE_ML = "key_water_intake_ml";
    private static final String KEY_MONITOR_ACTIVE = "key_monitor_active";
    private static final String KEY_DARK_MODE = "key_dark_mode";
    private static final String KEY_APP_LANGUAGE = "Locale.Helper.Selected.Language";

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // --- Authentication & Session ---

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void saveUserSession(long userId, String name, String emailOrPhone, long lmpMillis,
                                long babyBirthMillis, String bloodGroup, String emergencyPhone) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_MOTHER_NAME, name)
                .putString(KEY_USER_EMAIL_PHONE, emailOrPhone)
                .putLong(KEY_LMP_DATE_MILLIS, lmpMillis)
                .putLong(KEY_BABY_BIRTH_MILLIS, babyBirthMillis)
                .putString(KEY_BLOOD_GROUP, bloodGroup)
                .putString(KEY_EMERGENCY_PHONE, emergencyPhone)
                .apply();
    }

    public void logout() {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .putLong(KEY_USER_ID, -1)
                .apply();
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, 1);
    }

    public String getMotherName() {
        return prefs.getString(KEY_MOTHER_NAME, "Anna");
    }

    public void setMotherName(String name) {
        prefs.edit().putString(KEY_MOTHER_NAME, name).apply();
    }

    public String getUserEmailPhone() {
        return prefs.getString(KEY_USER_EMAIL_PHONE, "anna@maacare.org");
    }

    public String getBloodGroup() {
        return prefs.getString(KEY_BLOOD_GROUP, "O+");
    }

    public void setBloodGroup(String bloodGroup) {
        prefs.edit().putString(KEY_BLOOD_GROUP, bloodGroup).apply();
    }

    public String getEmergencyPhone() {
        return prefs.getString(KEY_EMERGENCY_PHONE, "+8801711223344");
    }

    public void setEmergencyPhone(String phone) {
        prefs.edit().putString(KEY_EMERGENCY_PHONE, phone).apply();
    }

    // --- Deterministic Gestational Timeline Engine (Clock-Driven) ---

    public long getLmpDateMillis() {
        // Default: 206 days ago = Week 30, Day 3
        long defaultLmp = System.currentTimeMillis() - (206L * 86400000L);
        return prefs.getLong(KEY_LMP_DATE_MILLIS, defaultLmp);
    }

    public void setLmpDateMillis(long millis) {
        prefs.edit().putLong(KEY_LMP_DATE_MILLIS, millis).apply();
    }

    /**
     * Exact number of elapsed days from LMP to current system time.
     */
    public long getDaysPregnant() {
        long elapsed = System.currentTimeMillis() - getLmpDateMillis();
        return Math.max(0, elapsed / 86400000L);
    }

    /**
     * Non-editable deterministic gestational week (1 to 42).
     */
    public int getCurrentWeek() {
        int week = (int) (getDaysPregnant() / 7) + 1;
        return Math.max(1, Math.min(42, week));
    }

    public int getGestationalWeek() {
        return getCurrentWeek();
    }

    /**
     * Day within current gestational week (0 to 6).
     */
    public int getDayOfWeek() {
        return (int) (getDaysPregnant() % 7);
    }

    /**
     * Countdown to Estimated Due Date (EDD = 280 days / 40 weeks from LMP).
     */
    public int getDaysRemainingToEdd() {
        return (int) Math.max(0, 280 - getDaysPregnant());
    }

    public int getTrimester() {
        int week = getCurrentWeek();
        if (week <= 12) return 1;
        if (week <= 27) return 2;
        return 3;
    }

    public String getGestationalFormatted() {
        return "Week " + getCurrentWeek() + ", Day " + getDayOfWeek();
    }

    // --- Child Chronological Age Tracker ---

    public long getBabyBirthMillis() {
        // Default: 56 days ago = 8 weeks old baby
        long defaultBirth = System.currentTimeMillis() - (56L * 86400000L);
        return prefs.getLong(KEY_BABY_BIRTH_MILLIS, defaultBirth);
    }

    public void setBabyBirthMillis(long millis) {
        prefs.edit().putLong(KEY_BABY_BIRTH_MILLIS, millis).apply();
    }

    public int getBabyAgeDays() {
        long elapsed = System.currentTimeMillis() - getBabyBirthMillis();
        return (int) Math.max(0, elapsed / 86400000L);
    }

    public int getBabyAgeWeeks() {
        return getBabyAgeDays() / 7;
    }

    public int getBabyAgeMonths() {
        return getBabyAgeDays() / 30;
    }

    public String getBabyAgeFormatted() {
        int weeks = getBabyAgeWeeks();
        int months = getBabyAgeMonths();
        return weeks + " Weeks (" + months + " Months old)";
    }

    // --- Editable Hydration Metrics & Goals ---

    public int getDailyWaterGoalMl() {
        return prefs.getInt(KEY_WATER_GOAL_ML, 3000);
    }

    public void setDailyWaterGoalMl(int goalMl) {
        prefs.edit().putInt(KEY_WATER_GOAL_ML, Math.max(500, goalMl)).apply();
    }

    public int getTodayWaterIntakeMl() {
        return prefs.getInt(KEY_WATER_INTAKE_ML, 1750);
    }

    public void setTodayWaterIntakeMl(int intakeMl) {
        prefs.edit().putInt(KEY_WATER_INTAKE_ML, Math.max(0, intakeMl)).apply();
    }

    public void adjustTodayWaterIntakeMl(int deltaMl) {
        setTodayWaterIntakeMl(getTodayWaterIntakeMl() + deltaMl);
    }

    public boolean isMonitorActive() {
        return prefs.getBoolean(KEY_MONITOR_ACTIVE, false);
    }

    public void setMonitorActive(boolean active) {
        prefs.edit().putBoolean(KEY_MONITOR_ACTIVE, active).apply();
    }

    // --- Theme & Localization Settings ---

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean darkMode) {
        prefs.edit().putBoolean(KEY_DARK_MODE, darkMode).apply();
    }

    public String getAppLanguage() {
        return prefs.getString(KEY_APP_LANGUAGE, "en");
    }

    public void setAppLanguage(String languageCode) {
        prefs.edit().putString(KEY_APP_LANGUAGE, languageCode).apply();
    }

    // --- Firebase UID & Sync Session ---

    public String getFirebaseUid() {
        return prefs.getString(KEY_FIREBASE_UID, "demo_anna_uid");
    }

    public void setFirebaseUid(String uid) {
        prefs.edit().putString(KEY_FIREBASE_UID, uid).apply();
    }

    // --- Contact Picker Info ---

    public String getEmergencyContactName() {
        return prefs.getString(KEY_EMERGENCY_CONTACT_NAME, "Dr. Rahman (Caregiver)");
    }

    public void setEmergencyContactName(String name) {
        prefs.edit().putString(KEY_EMERGENCY_CONTACT_NAME, name).apply();
    }

    // --- Media / Ultrasound & Baby Memory Photo ---

    public String getPhotoMemoryUri() {
        return prefs.getString(KEY_PHOTO_MEMORY_URI, null);
    }

    public void setPhotoMemoryUri(String uriString) {
        prefs.edit().putString(KEY_PHOTO_MEMORY_URI, uriString).apply();
    }
}
