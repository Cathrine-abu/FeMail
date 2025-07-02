package com.example.femail.labels;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LabelDao {

    @Query("SELECT * FROM labels WHERE :userId = :userId")
    LiveData<List<LabelItem>> getAllLabels(int userId);
    @Query("SELECT * FROM labels WHERE id = :id")
    LabelItem getLabel(int id);
    @Insert
    void insert(LabelItem... label);
    @Update
    void update(LabelItem label);
    @Delete
    void delete(LabelItem... label);
}
