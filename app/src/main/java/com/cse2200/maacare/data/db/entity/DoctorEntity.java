package com.cse2200.maacare.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Doctor contact and directory entity for maternal, obstetric, and pediatric care.
 */
@Entity(tableName = "doctors")
public class DoctorEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String specialty; // "Obstetrician", "Gynecologist", "Pediatrician"
    private String hospital;
    private double distanceKm;
    private String nextSlot;
    private boolean availableToday;
    private String phone;
    private double rating;

    public DoctorEntity(String name, String specialty, String hospital, double distanceKm,
                        String nextSlot, boolean availableToday, String phone, double rating) {
        this.name = name;
        this.specialty = specialty;
        this.hospital = hospital;
        this.distanceKm = distanceKm;
        this.nextSlot = nextSlot;
        this.availableToday = availableToday;
        this.phone = phone;
        this.rating = rating;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public String getNextSlot() { return nextSlot; }
    public void setNextSlot(String nextSlot) { this.nextSlot = nextSlot; }

    public boolean isAvailableToday() { return availableToday; }
    public void setAvailableToday(boolean availableToday) { this.availableToday = availableToday; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}
