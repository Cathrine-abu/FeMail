package com.example.femail.labels;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "labels")
public class LabelItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    public String userId;
    public String name;
    public LabelItem(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
    }
}
