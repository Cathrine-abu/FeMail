package com.example.femail;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class MailViewModel extends AndroidViewModel {
    private MailRepository repository;
    private LiveData<List<MailItem>> allMails;

    public MailViewModel(@NonNull Application application) {
        super(application);
        repository = new MailRepository(application);
        allMails = repository.getAllMails();
    }

    public LiveData<List<MailItem>> getAllMails() {
        return allMails;
    }

    public void insert(MailItem mail) {
        repository.insert(mail);
    }

    public void delete(MailItem mail) {
        repository.delete(mail);
    }

    public void deleteAll() {
        repository.deleteAll();
    }
}
