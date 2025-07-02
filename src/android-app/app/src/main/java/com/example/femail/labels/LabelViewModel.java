package com.example.femail.labels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class LabelViewModel extends AndroidViewModel {
    private LabelRepository repository;
    private LiveData<List<LabelItem>> allLabels;

    public LabelViewModel(@NonNull Application application) {
        super(application);
        repository = new LabelRepository(application);
    }

    public LiveData<List<LabelItem>> getAllLabels(String UserId) {
        return repository.getAllLabels(UserId);
    }

    public void insert(LabelItem label) {
        repository.insert(label);
    }

    public void delete(LabelItem label) {
        repository.delete(label);
    }

    public void update(LabelItem label) {
        repository.update(label);
    }

}