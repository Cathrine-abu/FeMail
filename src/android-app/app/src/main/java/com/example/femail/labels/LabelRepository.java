package com.example.femail.labels;

import android.app.Application;

import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LabelRepository {
    private LabelDao labelDao;
    private LiveData<List<LabelItem>> allLabels;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LabelRepository(Application application) {
        LabelDatabase db = LabelDatabase.getDatabase(application);
        labelDao = db.labelDao();
    }

    public LiveData<List<LabelItem>> getAllLabels(String userId) {
        return labelDao.getAllLabels(userId);
    }

    public void insert(LabelItem label) {
        executorService.execute(() -> labelDao.insert(label));
    }
    public void update(LabelItem label) {
        executorService.execute(() -> labelDao.update(label));
    }
    public void delete(LabelItem label) {
        executorService.execute(() -> labelDao.delete(label));
    }

}