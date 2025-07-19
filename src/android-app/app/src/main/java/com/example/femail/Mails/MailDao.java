package com.example.femail.Mails;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MailDao {

    @Query("SELECT * FROM mails WHERE userId = :userId ORDER BY time DESC")
    LiveData<List<MailItem>> getAllMails(String userId);
    
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    void insertMail(MailItem mail);
    
    @Delete
    void deleteMail(MailItem mail);
    
    @androidx.room.Update
    void updateMail(MailItem mail);

    @Query("SELECT * FROM mails WHERE userId = :userId AND [from] != 'me' AND subject LIKE '%win%'")
    List<MailItem> getSpamMails(String userId);

    @Query("SELECT * FROM mails WHERE userId = :userId AND [from] = 'me'")
    List<MailItem> getSentMails(String userId);

    // New specific queries for better performance
    @Query("SELECT * FROM mails WHERE userId = :userId AND (direction LIKE '%inbox%' OR direction LIKE '%received%') AND isDeleted = 0 AND isSpam = 0 ORDER BY time DESC")
    LiveData<List<MailItem>> getInboxMailsLive(String userId);
    
    @Query("SELECT * FROM mails WHERE userId = :userId AND direction LIKE '%sent%' AND isDeleted = 0 ORDER BY time DESC")
    LiveData<List<MailItem>> getSentMailsLive(String userId);
    
    @Query("SELECT * FROM mails WHERE userId = :userId AND isDraft = 1 AND isDeleted = 0 ORDER BY time DESC")
    LiveData<List<MailItem>> getDraftMailsLive(String userId);
    
    // Debug query to see all mails for a user
    @Query("SELECT * FROM mails WHERE userId = :userId ORDER BY time DESC")
    List<MailItem> getAllMailsDebug(String userId);
    
    @Query("SELECT * FROM mails WHERE userId = :userId AND isSpam = 1 AND isDeleted = 0 AND isDraft = 0 ORDER BY time DESC")
    LiveData<List<MailItem>> getSpamMailsLive(String userId);
    
    @Query("SELECT * FROM mails WHERE userId = :userId AND isStarred = 1 AND isDeleted = 0 ORDER BY time DESC")
    LiveData<List<MailItem>> getStarredMailsLive(String userId);
    
    @Query("SELECT * FROM mails WHERE userId = :userId AND isDeleted = 1 ORDER BY time DESC")
    LiveData<List<MailItem>> getTrashMailsLive(String userId);

    // New category queries
    @Query("SELECT * FROM mails WHERE userId = :userId AND category = 'primary' AND isDeleted = 0 ORDER BY time DESC")
    LiveData<List<MailItem>> getPrimaryMailsLive(String userId);
    
    @Query("SELECT * FROM mails WHERE userId = :userId AND category = 'social' AND isDeleted = 0 ORDER BY time DESC")
    LiveData<List<MailItem>> getSocialMailsLive(String userId);
    
    @Query("SELECT * FROM mails WHERE userId = :userId AND category = 'promotions' AND isDeleted = 0 ORDER BY time DESC")
    LiveData<List<MailItem>> getPromotionsMailsLive(String userId);
    
    @Query("SELECT * FROM mails WHERE userId = :userId AND category = 'updates' AND isDeleted = 0 ORDER BY time DESC")
    LiveData<List<MailItem>> getUpdatesMailsLive(String userId);

    @Query("DELETE FROM mails")
    void deleteAll();

    @Query("SELECT * FROM mails WHERE userId = :userId AND category = :labelName AND isDeleted = 0 ORDER BY time DESC")
    LiveData<List<MailItem>> getMailsByLabel(String labelName, String userId);

    @Query("SELECT * FROM mails WHERE userId = :userId AND (subject LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%') AND isDeleted = 0 ORDER BY time DESC")
    LiveData<List<MailItem>> searchMails(String query, String userId);

    @Query("SELECT * FROM mails WHERE id = :mailId LIMIT 1")
    MailItem getMailById(String mailId);

    @Query("SELECT * FROM mails WHERE userId = :userId ORDER BY time DESC LIMIT 1")
    MailItem getLatestMail(String userId);
    
    // Debug query specifically for drafts
    @Query("SELECT * FROM mails WHERE userId = :userId AND isDraft = 1 ORDER BY time DESC")
    List<MailItem> getDraftsDebug(String userId);
} 