package com.example.femail.Mails;

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
    
    @androidx.room.Update
    void updateMail(MailItem mail);

    @Query("SELECT * FROM mails WHERE [from] != 'me' AND isRead = 0")
    List<MailItem> getInboxMails();

    @Query("SELECT * FROM mails WHERE [from] != 'me' AND subject LIKE '%win%'")
    List<MailItem> getSpamMails();

    @Query("SELECT * FROM mails WHERE [from] = 'me'")
    List<MailItem> getSentMails();

    // New specific queries for better performance
    @Query("SELECT * FROM mails WHERE direction LIKE '%inbox%' AND isDeleted = 0")
    LiveData<List<MailItem>> getInboxMailsLive();
    
    @Query("SELECT * FROM mails WHERE direction LIKE '%sent%' AND isDeleted = 0")
    LiveData<List<MailItem>> getSentMailsLive();
    
    @Query("SELECT * FROM mails WHERE isDraft = 1 AND isDeleted = 0 AND direction LIKE '%draft%'")
    LiveData<List<MailItem>> getDraftMailsLive();
    
    @Query("SELECT * FROM mails WHERE isSpam = 1 AND isDeleted = 0 AND isDraft = 0")
    LiveData<List<MailItem>> getSpamMailsLive();
    
    @Query("SELECT * FROM mails WHERE isStarred = 1 AND isDeleted = 0")
    LiveData<List<MailItem>> getStarredMailsLive();
    
    @Query("SELECT * FROM mails WHERE isDeleted = 1")
    LiveData<List<MailItem>> getTrashMailsLive();

    // New category queries
    @Query("SELECT * FROM mails WHERE category = 'primary' AND isDeleted = 0")
    LiveData<List<MailItem>> getPrimaryMailsLive();
    
    @Query("SELECT * FROM mails WHERE category = 'social' AND isDeleted = 0")
    LiveData<List<MailItem>> getSocialMailsLive();
    
    @Query("SELECT * FROM mails WHERE category = 'promotions' AND isDeleted = 0")
    LiveData<List<MailItem>> getPromotionsMailsLive();
    
    @Query("SELECT * FROM mails WHERE category = 'updates' AND isDeleted = 0")
    LiveData<List<MailItem>> getUpdatesMailsLive();

    @Query("DELETE FROM mails")
    void deleteAll();

    @Query("SELECT * FROM mails WHERE category = :labelName AND isDeleted = 0")
    LiveData<List<MailItem>> getMailsByLabel(String labelName);

    @Query("SELECT * FROM mails WHERE (subject LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%') AND isDeleted = 0")
    LiveData<List<MailItem>> searchMails(String query);
} 