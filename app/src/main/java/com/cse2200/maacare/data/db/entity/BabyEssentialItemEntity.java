package com.cse2200.maacare.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Hospital bag and postpartum recovery essentials checklist item.
 */
@Entity(tableName = "baby_essentials")
public class BabyEssentialItemEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String category; // "Baby Essentials", "Labor & Delivery Bag", "Postpartum Recovery Kit"
    private String itemName;
    private boolean isChecked;

    public BabyEssentialItemEntity(String category, String itemName, boolean isChecked) {
        this.category = category;
        this.itemName = itemName;
        this.isChecked = isChecked;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }
}
