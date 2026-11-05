package com.cse2200.maacare.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity for logging newborn daily events (Feeding, Diaper changes, Sleep intervals, Tub bathing).
 */
@Entity(tableName = "baby_timeline")
public class BabyCareTimelineEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String dateString;
    private String timeFormatted;
    private long timestampMillis;
    private String activityType; // "FEEDING", "DIAPER", "SLEEP", "BATH"
    private String title;
    private String details;
    private String categoryColor;

    public BabyCareTimelineEntity(String dateString, String timeFormatted, long timestampMillis,
                                  String activityType, String title, String details, String categoryColor) {
        this.dateString = dateString;
        this.timeFormatted = timeFormatted;
        this.timestampMillis = timestampMillis;
        this.activityType = activityType;
        this.title = title;
        this.details = details;
        this.categoryColor = categoryColor;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDateString() { return dateString; }
    public void setDateString(String dateString) { this.dateString = dateString; }

    public String getTimeFormatted() { return timeFormatted; }
    public void setTimeFormatted(String timeFormatted) { this.timeFormatted = timeFormatted; }

    public long getTimestampMillis() { return timestampMillis; }
    public void setTimestampMillis(long timestampMillis) { this.timestampMillis = timestampMillis; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getCategoryColor() { return categoryColor; }
    public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }
}
