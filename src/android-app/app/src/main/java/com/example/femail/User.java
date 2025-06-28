package com.example.femail;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    public String username;
    @NonNull
    public String password;
    public String fullName;
    public String phone;
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