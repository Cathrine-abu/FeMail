package com.example.femail.Mails;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class MailViewModel extends AndroidViewModel {
    private final MailRepository repository;
    
    // Error and loading state management
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public MailViewModel(@NonNull Application application) {
        super(application);
        repository = new MailRepository(application);
    }

    public LiveData<List<MailItem>> getAllMails(String userId) {
        return repository.getAllMails(userId);
    }

    public LiveData<List<MailItem>> getInboxMails(String userId) {
        return repository.getInboxMails(userId);
    }

    public LiveData<List<MailItem>> getSentMails(String userId) {
        return repository.getSentMails(userId);
    }

    public LiveData<List<MailItem>> getDraftMails(String userId) {
        return repository.getDraftMails(userId);
    }

    public LiveData<List<MailItem>> getSpamMails(String userId) {
        return repository.getSpamMails(userId);
    }

    public LiveData<List<MailItem>> getStarredMails(String userId) {
        return repository.getStarredMails(userId);
    }

    public LiveData<List<MailItem>> getTrashMails(String userId) {
        return repository.getTrashMails(userId);
    }

    public LiveData<List<MailItem>> getPrimaryMails(String userId) {
        return repository.getPrimaryMails(userId);
    }

    public LiveData<List<MailItem>> getSocialMails(String userId) {
        return repository.getSocialMails(userId);
    }

    public LiveData<List<MailItem>> getPromotionsMails(String userId) {
        return repository.getPromotionsMails(userId);
    }

    public LiveData<List<MailItem>> getUpdatesMails(String userId) {
        return repository.getUpdatesMails(userId);
    }

    public LiveData<List<MailItem>> getMailsByLabel(String labelName, String userId) {
        return repository.getMailsByLabel(labelName, userId);
    }

    public LiveData<List<MailItem>> searchMails(String query, String userId) {
        return repository.searchMails(query, userId);
    }

    // Error and loading state getters
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    public void insert(MailItem mail) {
        repository.insert(mail);
    }

    public void delete(MailItem mail) {
        repository.delete(mail);
    }

    public void update(MailItem mail) {
        repository.update(mail);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public void deleteMailPermanently(String token, String userId, String mailId) {
        repository.deleteMailPermanently(token, userId, mailId);
    }

    public void sendMailToServer(Context context, String token, String userId, MailItem mail) {
        repository.sendMailToServer(token, userId, mail);
    }

    public void markMailAsSpamOnServer(String token, String userId, String mailId) {
        repository.markMailAsSpam(token, userId, mailId);
    }

    public void unmarkMailAsSpamOnServer(String token, String userId, String mailId) {
        repository.unmarkMailAsSpam(token, userId, mailId);
    }
} 