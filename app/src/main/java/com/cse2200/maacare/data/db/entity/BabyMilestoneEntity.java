package com.cse2200.maacare.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Developmental milestone item with Yes/No status and optional clinical observation notes.
 */
@Entity(tableName = "baby_milestones")
public class BabyMilestoneEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String category; // "Motor Skills", "Social & Communication", "Cognitive & Vision"
    private String questionText;
    private boolean isAchieved;
    private String remarkText;
    private long lastUpdatedMillis;

    public BabyMilestoneEntity(String category, String questionText, boolean isAchieved,
                               String remarkText, long lastUpdatedMillis) {
        this.category = category;
        this.questionText = questionText;
        this.isAchieved = isAchieved;
        this.remarkText = remarkText;
        this.lastUpdatedMillis = lastUpdatedMillis;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public boolean isAchieved() { return isAchieved; }
    public void setAchieved(boolean achieved) { isAchieved = achieved; }

    public String getRemarkText() { return remarkText; }
    public void setRemarkText(String remarkText) { this.remarkText = remarkText; }

    public long getLastUpdatedMillis() { return lastUpdatedMillis; }
    public void setLastUpdatedMillis(long lastUpdatedMillis) { this.lastUpdatedMillis = lastUpdatedMillis; }
}
