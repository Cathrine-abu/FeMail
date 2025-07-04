package com.example.femail.labels;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.femail.Mails.MailItem;

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
    public LabelItem getLabelByName(String labelName) {
        return repository.getLabelByName(labelName);
    }

    public void refreshLabels(String token, String userId) {
        repository.fetchLabelsFromServer(token, userId);
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

    public void sendLabelToServer(Context context, String token, String userId, LabelItem label) {
        repository.sendLabelToServer(token, userId, label);
    }

    public void updateLabelOnServer(Context context, String token, String userId, int labelId, LabelItem updatedLabel) {
        repository.updateLabelOnServer(token, userId, labelId, updatedLabel);
    }

    public void deleteLabelOnServer(Context context, String token, String userId, int labelId) {
        repository.deleteLabelOnServer(token, userId, labelId);
    }
}