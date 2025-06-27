package com.example.femail;

public class MailItem {
    public String subject;
    public String time;

    public String body;
    public boolean isStarred;
    public boolean isSelected = false;

    public String sender;

    public MailItem(String subject, String time, String body, boolean isStarred, boolean isSelected, String sender) {
        this.subject = subject;
        this.time = time;
        this.body = body;
        this.isStarred = isStarred;
        this.isSelected = isSelected;
        this.sender = sender;
    }

    public String getSubject() {
        return subject;
    }

    public  String getBody(){
        return body;
    }
    public String getTime() {
        return time;
    }

    public boolean getStarred() {
        return isStarred;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public String getSender() {
        return sender;
    }
}
