package com.example.femail;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    public String username;
    @NonNull
    public String password;
    @SerializedName("full_name")
    @ColumnInfo(name = "full_name")
    public String fullName;
    public String phone;
    @SerializedName("birth_date")
    @ColumnInfo(name = "birth_date")
    public String birthDate;
    public String gender;
    public String image;

    public User(@NonNull String username, @NonNull String password, String fullName, String phone, String birthDate, String gender, String image) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
        this.image = image;
    }
} 