package com.cse2200.maacare.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.cse2200.maacare.data.db.MaaCareDatabase;
import com.cse2200.maacare.data.db.entity.MedicineEntity;
import com.cse2200.maacare.data.db.entity.VaccinationEntity;

/**
 * Standard Android ContentProvider enabling companion applications, caregivers,
 * or desktop widgets to securely query maternal medication adherence and vaccination records.
 */
public class MaaCareContentProvider extends ContentProvider {

    private static final int CODE_MEDICINES = 100;
    private static final int CODE_MEDICINE_ID = 101;
    private static final int CODE_VACCINATIONS = 200;
    private static final int CODE_VACCINATION_ID = 201;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(MaaCareContract.AUTHORITY, MaaCareContract.PATH_MEDICINES, CODE_MEDICINES);
        URI_MATCHER.addURI(MaaCareContract.AUTHORITY, MaaCareContract.PATH_MEDICINES + "/#", CODE_MEDICINE_ID);
        URI_MATCHER.addURI(MaaCareContract.AUTHORITY, MaaCareContract.PATH_VACCINATIONS, CODE_VACCINATIONS);
        URI_MATCHER.addURI(MaaCareContract.AUTHORITY, MaaCareContract.PATH_VACCINATIONS + "/#", CODE_VACCINATION_ID);
    }

    private MaaCareDatabase database;

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            database = MaaCareDatabase.getInstance(getContext());
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = URI_MATCHER.match(uri);
        Cursor cursor;

        switch (match) {
            case CODE_MEDICINES:
                cursor = database.medicineDao().selectAllCursor();
                break;
            case CODE_MEDICINE_ID:
                long medId = ContentUris.parseId(uri);
                cursor = database.medicineDao().selectByIdCursor(medId);
                break;
            case CODE_VACCINATIONS:
                cursor = database.vaccinationDao().selectAllCursor();
                break;
            case CODE_VACCINATION_ID:
                long vacId = ContentUris.parseId(uri);
                cursor = database.vaccinationDao().selectByIdCursor(vacId);
                break;
            default:
                throw new IllegalArgumentException("Unknown Query URI: " + uri);
        }

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = URI_MATCHER.match(uri);
        switch (match) {
            case CODE_MEDICINES:
                return MaaCareContract.MedicineEntry.CONTENT_TYPE;
            case CODE_MEDICINE_ID:
                return MaaCareContract.MedicineEntry.CONTENT_ITEM_TYPE;
            case CODE_VACCINATIONS:
                return MaaCareContract.VaccinationEntry.CONTENT_TYPE;
            case CODE_VACCINATION_ID:
                return MaaCareContract.VaccinationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown MIME Type URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (values == null) {
            throw new IllegalArgumentException("ContentValues cannot be null");
        }

        int match = URI_MATCHER.match(uri);
        long insertedId;

        switch (match) {
            case CODE_MEDICINES:
                MedicineEntity med = new MedicineEntity();
                med.setName(values.getAsString(MaaCareContract.MedicineEntry.COLUMN_NAME));
                med.setDosage(values.getAsString(MaaCareContract.MedicineEntry.COLUMN_DOSAGE));
                med.setReminderTime(values.getAsString(MaaCareContract.MedicineEntry.COLUMN_REMINDER_TIME));
                med.setTakenToday(values.getAsBoolean(MaaCareContract.MedicineEntry.COLUMN_IS_TAKEN) != null
                        && values.getAsBoolean(MaaCareContract.MedicineEntry.COLUMN_IS_TAKEN));
                med.setReminderEnabled(values.getAsBoolean(MaaCareContract.MedicineEntry.COLUMN_REMINDER_ENABLED) != null
                        && values.getAsBoolean(MaaCareContract.MedicineEntry.COLUMN_REMINDER_ENABLED));
                insertedId = database.medicineDao().insert(med);
                break;

            case CODE_VACCINATIONS:
                VaccinationEntity vac = new VaccinationEntity();
                vac.setVaccineName(values.getAsString(MaaCareContract.VaccinationEntry.COLUMN_NAME));
                vac.setTargetAge(values.getAsString(MaaCareContract.VaccinationEntry.COLUMN_TARGET_AGE));
                vac.setDiseaseTarget(values.getAsString(MaaCareContract.VaccinationEntry.COLUMN_DISEASE_TARGET));
                vac.setDone(values.getAsBoolean(MaaCareContract.VaccinationEntry.COLUMN_IS_DONE) != null
                        && values.getAsBoolean(MaaCareContract.VaccinationEntry.COLUMN_IS_DONE));
                insertedId = database.vaccinationDao().insert(vac);
                break;

            default:
                throw new IllegalArgumentException("Unsupported insert URI: " + uri);
        }

        if (insertedId > 0 && getContext() != null) {
            Uri itemUri = ContentUris.withAppendedId(uri, insertedId);
            getContext().getContentResolver().notifyChange(uri, null);
            return itemUri;
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = URI_MATCHER.match(uri);
        int rowsDeleted;

        switch (match) {
            case CODE_MEDICINE_ID:
                long medId = ContentUris.parseId(uri);
                rowsDeleted = database.medicineDao().deleteById(medId);
                break;
            case CODE_VACCINATION_ID:
                long vacId = ContentUris.parseId(uri);
                rowsDeleted = database.vaccinationDao().deleteById(vacId);
                break;
            default:
                throw new IllegalArgumentException("Delete unsupported for URI: " + uri);
        }

        if (rowsDeleted > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        if (values == null) return 0;
        int match = URI_MATCHER.match(uri);
        int rowsUpdated = 0;

        switch (match) {
            case CODE_MEDICINE_ID:
                long medId = ContentUris.parseId(uri);
                MedicineEntity med = database.medicineDao().getMedicineById(medId);
                if (med != null) {
                    if (values.containsKey(MaaCareContract.MedicineEntry.COLUMN_NAME)) {
                        med.setName(values.getAsString(MaaCareContract.MedicineEntry.COLUMN_NAME));
                    }
                    if (values.containsKey(MaaCareContract.MedicineEntry.COLUMN_IS_TAKEN)) {
                        med.setTakenToday(values.getAsBoolean(MaaCareContract.MedicineEntry.COLUMN_IS_TAKEN));
                    }
                    if (values.containsKey(MaaCareContract.MedicineEntry.COLUMN_REMINDER_ENABLED)) {
                        med.setReminderEnabled(values.getAsBoolean(MaaCareContract.MedicineEntry.COLUMN_REMINDER_ENABLED));
                    }
                    rowsUpdated = database.medicineDao().update(med);
                }
                break;

            case CODE_VACCINATION_ID:
                long vacId = ContentUris.parseId(uri);
                VaccinationEntity vac = database.vaccinationDao().getVaccineById(vacId);
                if (vac != null) {
                    if (values.containsKey(MaaCareContract.VaccinationEntry.COLUMN_IS_DONE)) {
                        vac.setDone(values.getAsBoolean(MaaCareContract.VaccinationEntry.COLUMN_IS_DONE));
                    }
                    rowsUpdated = database.vaccinationDao().update(vac);
                }
                break;

            default:
                throw new IllegalArgumentException("Update unsupported for URI: " + uri);
        }

        if (rowsUpdated > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
