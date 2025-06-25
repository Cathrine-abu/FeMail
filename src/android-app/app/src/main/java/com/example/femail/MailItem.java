package com.example.femail;

public class MailItem {
    public String subject;
    public String time;
    public boolean isStarred;

    public MailItem(String subject, String time, boolean isStarred) {
        this.subject = subject;
        this.time = time;
        this.isStarred = isStarred;
    }

    public String getSubject() {
        return subject;
    }

    public String getTime() {
        return time;
    }
}
