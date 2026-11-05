package com.cse2200.maacare.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Health check & vital statistics record for temperature, pulse, SpO2, and sickness symptoms.
 */
@Entity(tableName = "baby_vitals")
public class BabyVitalsEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private boolean isSick;
    private String symptomsNotes;
    private double tempStart;
    private double tempEnd;
    private String tempDegreeOrFreq; // e.g. "98.6°F - Normal"
    private int pulseFrequency;      // e.g. 120 bpm
    private int spo2Frequency;       // e.g. 98 %
    private long recordedMillis;

    public BabyVitalsEntity(boolean isSick, String symptomsNotes, double tempStart, double tempEnd,
                            String tempDegreeOrFreq, int pulseFrequency, int spo2Frequency, long recordedMillis) {
        this.isSick = isSick;
        this.symptomsNotes = symptomsNotes;
        this.tempStart = tempStart;
        this.tempEnd = tempEnd;
        this.tempDegreeOrFreq = tempDegreeOrFreq;
        this.pulseFrequency = pulseFrequency;
        this.spo2Frequency = spo2Frequency;
        this.recordedMillis = recordedMillis;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public boolean isSick() { return isSick; }
    public void setSick(boolean sick) { isSick = sick; }

    public String getSymptomsNotes() { return symptomsNotes; }
    public void setSymptomsNotes(String symptomsNotes) { this.symptomsNotes = symptomsNotes; }

    public double getTempStart() { return tempStart; }
    public void setTempStart(double tempStart) { this.tempStart = tempStart; }

    public double getTempEnd() { return tempEnd; }
    public void setTempEnd(double tempEnd) { this.tempEnd = tempEnd; }

    public String getTempDegreeOrFreq() { return tempDegreeOrFreq; }
    public void setTempDegreeOrFreq(String tempDegreeOrFreq) { this.tempDegreeOrFreq = tempDegreeOrFreq; }

    public int getPulseFrequency() { return pulseFrequency; }
    public void setPulseFrequency(int pulseFrequency) { this.pulseFrequency = pulseFrequency; }

    public int getSpo2Frequency() { return spo2Frequency; }
    public void setSpo2Frequency(int spo2Frequency) { this.spo2Frequency = spo2Frequency; }

    public long getRecordedMillis() { return recordedMillis; }
    public void setRecordedMillis(long recordedMillis) { this.recordedMillis = recordedMillis; }
}
