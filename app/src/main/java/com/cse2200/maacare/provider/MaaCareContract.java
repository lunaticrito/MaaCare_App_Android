package com.cse2200.maacare.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Public Contract defining Content URIs, MIME types, and Column names
 * for external access to MaaCare maternal health data.
 */
public final class MaaCareContract {

    public static final String AUTHORITY = "com.cse2200.maacare.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_MEDICINES = "medicines";
    public static final String PATH_VACCINATIONS = "vaccinations";
    public static final String PATH_HYDRATION = "hydration";

    private MaaCareContract() {
        // Prevent instantiation
    }

    public static final class MedicineEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MEDICINES);

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PATH_MEDICINES;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + PATH_MEDICINES;

        public static final String TABLE_NAME = "medicines";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DOSAGE = "dosage";
        public static final String COLUMN_REMINDER_TIME = "reminder_time";
        public static final String COLUMN_START_DATE = "start_date_millis";
        public static final String COLUMN_DURATION_DAYS = "duration_days";
        public static final String COLUMN_END_DATE = "end_date_millis";
        public static final String COLUMN_IS_TAKEN = "is_taken_today";
        public static final String COLUMN_REMINDER_ENABLED = "reminder_enabled";
    }

    public static final class VaccinationEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VACCINATIONS);

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PATH_VACCINATIONS;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + PATH_VACCINATIONS;

        public static final String TABLE_NAME = "vaccinations";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "vaccine_name";
        public static final String COLUMN_TARGET_AGE = "target_age";
        public static final String COLUMN_TARGET_AGE_DAYS = "target_age_days";
        public static final String COLUMN_DISEASE_TARGET = "disease_target";
        public static final String COLUMN_IS_DONE = "is_done";
    }

    public static final class HydrationEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_HYDRATION);

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PATH_HYDRATION;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + PATH_HYDRATION;

        public static final String TABLE_NAME = "hydration_logs";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_DATE = "date_string";
        public static final String COLUMN_VOLUME_ML = "volume_ml";
        public static final String COLUMN_LOGGED_TIME = "logged_time_millis";
    }
}
