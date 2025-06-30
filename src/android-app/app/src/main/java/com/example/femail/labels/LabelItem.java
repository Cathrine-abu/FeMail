package com.example.femail.labels;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "labels")
public class LabelItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public LabelItem(String name) {
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
    }
}
