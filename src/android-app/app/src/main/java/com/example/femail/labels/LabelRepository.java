package com.example.femail.labels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.femail.Mails.MailItem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LabelRepository {
    private LabelDao labelDao;
    private LiveData<List<LabelItem>> allLabels;
    private final Gson gson = new Gson();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LabelRepository(Application application) {
        LabelDatabase db = LabelDatabase.getDatabase(application);
        labelDao = db.labelDao();
    }

    public LiveData<List<LabelItem>> getAllLabels(String userId) {
        return labelDao.getAllLabels(userId);
    }

    // --- ROOM ---
    public void insert(LabelItem label) {
        executorService.execute(() -> labelDao.insert(label));
    }
    public void update(LabelItem label) {
        executorService.execute(() -> labelDao.update(label));
    }
    public void delete(LabelItem label) {
        executorService.execute(() -> labelDao.delete(label));
    }

    // --- SERVER ---
    public LiveData<List<LabelItem>> fetchLabelsFromServer(String token, String userId) {
        MutableLiveData<List<LabelItem>> labelLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/api/labels/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("user-id", userId);
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                Type listType = new TypeToken<List<LabelItem>>() {}.getType();
                List<LabelItem> labels = gson.fromJson(result.toString(), listType);
                for (LabelItem label : labels) {
                    label.setUserId(userId);
                }
                labelLiveData.postValue(labels);

                // Save data to Room database
                if (labels != null && !labels.isEmpty()) {
                    // Clear old data and insert new one
                    labelDao.deleteAll();
                    labelDao.insert(labels.toArray(new LabelItem[0]));
                }

            } catch (Exception e) {
                Log.e("LabelRepository", "fetchLabelsFromServer error", e);
            }
        });
        return labelLiveData;
    }

    public void sendLabelToServer(String token, String userId, LabelItem label) {
        executorService.execute(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/api/labels");
                HttpURLConnection conn = setupConnection(url, "POST", token, userId);

                String json = gson.toJson(label);
                writeJson(conn, json);

                if (conn.getResponseCode() != 200) {
                    Log.e("LabelRepository", "sendLabel failed: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                Log.e("LabelRepository", "sendLabel error", e);
            }
        });
    }

    public void updateLabelOnServer(String token, String userId, int labelId, LabelItem updatedLabel) {
        executorService.execute(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/api/labels/" + labelId);
                HttpURLConnection conn = setupConnection(url, "PATCH", token, userId);

                String json = gson.toJson(updatedLabel);
                writeJson(conn, json);

                if (conn.getResponseCode() != 200) {
                    Log.e("LabelRepository", "updateLabel failed: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                Log.e("LabelRepository", "updateLabel error", e);
            }
        });
    }

    public void deleteLabelOnServer(String token, String userId, int labelId) {
        executorService.execute(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/api/labels/" + labelId);
                HttpURLConnection conn = setupConnection(url, "DELETE", token, userId);
                conn.connect();

                if (conn.getResponseCode() != 200 && conn.getResponseCode() != 204) {
                    Log.e("LabelRepository", "deletePermanently failed: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                Log.e("LabelRepository", "deletePermanently error", e);
            }
        });
    }

    private HttpURLConnection setupConnection(URL url, String method, String token, String userId) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("user-id", userId);
        conn.setRequestMethod(method);
        conn.setDoOutput(true);
        return conn;
    }

    private void writeJson(HttpURLConnection conn, String json) throws Exception {
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
    }
}