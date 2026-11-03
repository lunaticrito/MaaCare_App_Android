package com.cse2200.maacare.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing an individual water/fluid intake event for daily hydration tracking.
 */
@Entity(tableName = "hydration_logs")
public class HydrationLogEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "date_string")
    private String dateString; // e.g. "2026-11-03"

    @ColumnInfo(name = "volume_ml")
    private int volumeMl;

    @ColumnInfo(name = "logged_time_millis")
    private long loggedTimeMillis;

    public HydrationLogEntity() {
    }

    public HydrationLogEntity(String dateString, int volumeMl, long loggedTimeMillis) {
        this.dateString = dateString;
        this.volumeMl = volumeMl;
        this.loggedTimeMillis = loggedTimeMillis;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public int getVolumeMl() {
        return volumeMl;
    }

    public void setVolumeMl(int volumeMl) {
        this.volumeMl = volumeMl;
    }

    public long getLoggedTimeMillis() {
        return loggedTimeMillis;
    }

    public void setLoggedTimeMillis(long loggedTimeMillis) {
        this.loggedTimeMillis = loggedTimeMillis;
    }
}
