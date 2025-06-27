package com.example.femail;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Entity(tableName = "mails")
public class MailItem {
    @PrimaryKey @NonNull
    public String id;

    public String subject;
    public String body;
    public String from;
    public List<String> to;
    public String time;
    public boolean isStarred;
    public boolean isRead;
    public boolean isSpam;
    public boolean isDraft;
    public List<String> direction;
    public boolean isSelected = false;


    public MailItem(@NonNull String id, String subject, String body, String from, List<String> to,
                    String time, boolean isStarred, boolean isRead,
                    boolean isSpam, boolean isDraft, List<String> direction, boolean isSelected) {
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
        this.direction = direction;
        this.isSelected = isSelected;
    }
    @Ignore
    public MailItem(@NonNull String id, String subject, String body, String from, String time) {
        this.id = id;
        this.subject = subject;
        this.body = body;
        this.from = from;
        this.time = time;
    }
    @Ignore
    public MailItem(String from, String subject, Date date, String category) {
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
        this.direction = List.of(category);
        this.isSelected = false;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getTime() {
        return time;
    }

    public boolean getStarred() {
        return isStarred;
    }

    public String getSender() {
        return from;
    }

}
