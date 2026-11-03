package com.cse2200.maacare.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing an EPI & WHO compliant newborn vaccination milestone.
 */
@Entity(tableName = "vaccinations")
public class VaccinationEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "vaccine_name")
    private String vaccineName;

    @ColumnInfo(name = "target_age")
    private String targetAge; // e.g. "At Birth", "6 Weeks", "10 Weeks", "14 Weeks", "9 Months", "15 Months"

    @ColumnInfo(name = "target_age_days")
    private int targetAgeDays;

    @ColumnInfo(name = "disease_target")
    private String diseaseTarget;

    @ColumnInfo(name = "is_done")
    private boolean isDone;

    @ColumnInfo(name = "administered_date")
    private String administeredDate;

    public VaccinationEntity() {
    }

    public VaccinationEntity(String vaccineName, String targetAge, int targetAgeDays,
                             String diseaseTarget, boolean isDone, String administeredDate) {
        this.vaccineName = vaccineName;
        this.targetAge = targetAge;
        this.targetAgeDays = targetAgeDays;
        this.diseaseTarget = diseaseTarget;
        this.isDone = isDone;
        this.administeredDate = administeredDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public String getTargetAge() {
        return targetAge;
    }

    public void setTargetAge(String targetAge) {
        this.targetAge = targetAge;
    }

    public int getTargetAgeDays() {
        return targetAgeDays;
    }

    public void setTargetAgeDays(int targetAgeDays) {
        this.targetAgeDays = targetAgeDays;
    }

    public String getDiseaseTarget() {
        return diseaseTarget;
    }

    public void setDiseaseTarget(String diseaseTarget) {
        this.diseaseTarget = diseaseTarget;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public String getAdministeredDate() {
        return administeredDate;
    }

    public void setAdministeredDate(String administeredDate) {
        this.administeredDate = administeredDate;
    }

    /**
     * Determines whether the vaccine is Completed, Due Now, or Upcoming based on baby's age in days.
     */
    public String computeStatus(int babyAgeDays) {
        if (isDone) {
            return "COMPLETED";
        } else if (babyAgeDays >= targetAgeDays) {
            return "DUE NOW";
        } else {
            return "UPCOMING";
        }
    }
}
