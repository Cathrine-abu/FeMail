package com.example.femail;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.femail.MailDao;
import com.example.femail.MailDatabase;
import com.example.femail.MailItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailRepository {
    private MailDao mailDao;
    private LiveData<List<MailItem>> allMails;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public MailRepository(Application application) {
        MailDatabase db = MailDatabase.getDatabase(application);
        mailDao = db.mailDao();
        allMails = mailDao.getAllMails();
    }

    public LiveData<List<MailItem>> getAllMails() {
        return allMails;
    }

    public void insert(MailItem mail) {
        executorService.execute(() -> mailDao.insertMail(mail));
    }

    public void delete(MailItem mail) {
        executorService.execute(() -> mailDao.deleteMail(mail));
    }

    public void deleteAll() {
        executorService.execute(mailDao::deleteAll);
    }
}
