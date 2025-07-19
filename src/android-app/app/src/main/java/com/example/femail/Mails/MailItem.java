package com.example.femail.Mails;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.femail.Converters;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Entity(tableName = "mails")
public class MailItem {
    @PrimaryKey
    @NonNull
    public String id;

    public String subject;
    public String body;
    public String from;
    @TypeConverters(Converters.class)
    public List<String> to;
    public String owner;
    public String user;
    public String groupId;
    public String category;
    public String time;
    
    // Add timestamp field to handle backend response
    @SerializedName("timestamp")
    @Ignore
    public String timestamp;
    
    public boolean isStarred;
    public boolean isRead;
    public boolean isSpam;
    public boolean isDraft;
    public boolean isDeleted;
    @TypeConverters(Converters.class)
    public List<String> direction;

    @TypeConverters(Converters.class)
    public List<String> previousDirection;

    public boolean isSelected = false;

    public String userId;

    public MailItem(@NonNull String id, String subject, String body, String from, List<String> to,
                    String time, boolean isStarred, boolean isRead,
                    boolean isSpam, boolean isDraft, boolean isDeleted,
                    List<String> direction, String user, String owner,
                    String groupId, String category, boolean isSelected, String userId) {
        this.id = id;
        this.subject = subject;
        this.body = body;
        this.from = from;
        this.to = to;
        this.time = time;
        this.isStarred = isStarred;
        this.isRead = isRead;
        this.isSpam = isSpam;
        this.isDraft = isDraft;
        this.isDeleted = isDeleted;
        this.direction = direction;
        this.user = user;
        this.owner = owner;
        this.groupId = groupId;
        this.category = category;
        this.isSelected = isSelected;
        this.userId = userId;
    }

    @Ignore
    public MailItem(String from, String subject, Date date, String category, String userId) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.from = from;
        this.subject = subject;
        this.body = "";
        this.to = null;
        this.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date);
        this.isStarred = false;
        this.isRead = false;
        this.isSpam = category.equals("spam");
        this.isDraft = category.equals("draft");
        this.isDeleted = false;
        this.direction = List.of(category);
        this.user = "";
        this.owner = "";
        this.groupId = null;
        this.category = category;
        this.isSelected = false;
        this.userId = userId;
    }

    // Method to convert timestamp to time format for sorting
    public void convertTimestampToTime() {
        if (timestamp != null && !timestamp.isEmpty() && (time == null || time.isEmpty())) {
            // Convert ISO timestamp to the format expected by the app
            try {
                // Try multiple timestamp formats
                java.util.Date date = null;
                java.text.SimpleDateFormat[] formats = {
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()),
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()),
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.getDefault()),
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.getDefault()),
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()),
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                };
                
                for (java.text.SimpleDateFormat format : formats) {
                    try {
                        date = format.parse(timestamp);
                        break;
                    } catch (Exception e) {
                        // Continue to next format
                    }
                }
                
                if (date != null) {
                    java.text.SimpleDateFormat appFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                    this.time = appFormat.format(date);
                    android.util.Log.d("MailItem", "Successfully converted timestamp: " + timestamp + " -> " + this.time);
                } else {
                    // If all parsing fails, use timestamp as is
                    this.time = timestamp;
                    android.util.Log.w("MailItem", "Failed to parse timestamp, using as-is: " + timestamp);
                }
            } catch (Exception e) {
                android.util.Log.e("MailItem", "Error converting timestamp: " + timestamp, e);
                // If all parsing fails, use timestamp as is
                this.time = timestamp;
            }
        }
    }

    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getTime() { return time; }
    public boolean getStarred() { return isStarred; }
    public String getSender() { return from; }

    public String getId() {
        return id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}