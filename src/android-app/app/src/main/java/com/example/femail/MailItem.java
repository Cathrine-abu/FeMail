package com.example.femail;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

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
    public List<String> to;
    public String owner;
    public String user;
    public String groupId;
    public String category;
    public String time;
    public boolean isStarred;
    public boolean isRead;
    public boolean isSpam;
    public boolean isDraft;
    public boolean isDeleted;
    public List<String> direction;

    public boolean isSelected = false;

    public MailItem(@NonNull String id, String subject, String body, String from, List<String> to,
                    String time, boolean isStarred, boolean isRead,
                    boolean isSpam, boolean isDraft, boolean isDeleted,
                    List<String> direction, String user, String owner,
                    String groupId, String category, boolean isSelected) {
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
        this.isDeleted = false;
        this.direction = List.of(category);
        this.user = "";
        this.owner = "";
        this.groupId = null;
        this.category = category;
        this.isSelected = false;
    }

    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getTime() { return time; }
    public boolean getStarred() { return isStarred; }
    public String getSender() { return from; }
}
