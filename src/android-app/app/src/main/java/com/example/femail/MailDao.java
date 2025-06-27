package com.example.femail;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MailDao {

    @Query("SELECT * FROM mails")
    LiveData<List<MailItem>> getAllMails();
    @Insert
    void insertMail(MailItem mail);
    @Delete
    void deleteMail(MailItem mail);

    @Query("SELECT * FROM mails WHERE [from] != 'me' AND isRead = 0")
    List<MailItem> getInboxMails();

    @Query("SELECT * FROM mails WHERE [from] != 'me' AND subject LIKE '%win%'")
    List<MailItem> getSpamMails();

    @Query("SELECT * FROM mails WHERE [from] = 'me'")
    List<MailItem> getSentMails();

    @Query("DELETE FROM mails")
    void deleteAll();
}
