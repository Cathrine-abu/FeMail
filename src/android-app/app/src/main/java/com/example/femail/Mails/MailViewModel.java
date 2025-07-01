package com.example.femail.Mails;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class MailViewModel extends AndroidViewModel {
    private MailRepository repository;
    private LiveData<List<MailItem>> allMails;
    
    // Error and loading state management
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public MailViewModel(@NonNull Application application) {
        super(application);
        repository = new MailRepository(application);
        allMails = repository.getAllMails();
    }

    public LiveData<List<MailItem>> getAllMails() {
        return allMails;
    }

    // New specific mail type methods
    public LiveData<List<MailItem>> getInboxMails() {
        return repository.getInboxMails();
    }

    public LiveData<List<MailItem>> getSentMails() {
        return repository.getSentMails();
    }

    public LiveData<List<MailItem>> getDraftMails() {
        return repository.getDraftMails();
    }

    public LiveData<List<MailItem>> getSpamMails() {
        return repository.getSpamMails();
    }

    public LiveData<List<MailItem>> getStarredMails() {
        return repository.getStarredMails();
    }

    public LiveData<List<MailItem>> getTrashMails() {
        return repository.getTrashMails();
    }

    // New category methods
    public LiveData<List<MailItem>> getPrimaryMails() {
        return repository.getPrimaryMails();
    }

    public LiveData<List<MailItem>> getSocialMails() {
        return repository.getSocialMails();
    }

    public LiveData<List<MailItem>> getPromotionsMails() {
        return repository.getPromotionsMails();
    }

    public LiveData<List<MailItem>> getUpdatesMails() {
        return repository.getUpdatesMails();
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
        isLoading.setValue(true);
        try {
            repository.insert(mail);
            isLoading.setValue(false);
        } catch (Exception e) {
            errorMessage.setValue("Failed to insert mail: " + e.getMessage());
            isLoading.setValue(false);
        }
    }

    public void delete(MailItem mail) {
        isLoading.setValue(true);
        try {
            repository.delete(mail);
            isLoading.setValue(false);
        } catch (Exception e) {
            errorMessage.setValue("Failed to delete mail: " + e.getMessage());
            isLoading.setValue(false);
        }
    }

    public void update(MailItem mail) {
        isLoading.setValue(true);
        try {
            repository.update(mail);
            isLoading.setValue(false);
        } catch (Exception e) {
            errorMessage.setValue("Failed to update mail: " + e.getMessage());
            isLoading.setValue(false);
        }
    }

    public void deleteAll() {
        isLoading.setValue(true);
        try {
            repository.deleteAll();
            isLoading.setValue(false);
        } catch (Exception e) {
            errorMessage.setValue("Failed to delete all mails: " + e.getMessage());
            isLoading.setValue(false);
        }
    }

    public void deleteMailPermanently(String token, String userId, String mailId) {
        repository.deleteMailPermanently(token, userId, mailId);
    }

    public LiveData<List<MailItem>> getMailsByLabel(String labelName) {
        return repository.getMailsByLabel(labelName);
    }

    public LiveData<List<MailItem>> searchMails(String query) {
        return repository.searchMails(query);
    }
} 