package com.example.femail;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailRepository {

    private final MailDao mailDao;
    private final LiveData<List<MailItem>> allMails;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    public MailRepository(Application application) {
        MailDatabase db = MailDatabase.getDatabase(application);
        mailDao = db.mailDao();
        allMails = mailDao.getAllMails();
    }

    // --- ROOM ---
    public LiveData<List<MailItem>> getAllMails() {
        return allMails;
    }

    // New specific mail type methods
    public LiveData<List<MailItem>> getInboxMails() {
        return mailDao.getInboxMailsLive();
    }

    public LiveData<List<MailItem>> getSentMails() {
        return mailDao.getSentMailsLive();
    }

    public LiveData<List<MailItem>> getDraftMails() {
        return mailDao.getDraftMailsLive();
    }

    public LiveData<List<MailItem>> getSpamMails() {
        return mailDao.getSpamMailsLive();
    }

    public LiveData<List<MailItem>> getStarredMails() {
        return mailDao.getStarredMailsLive();
    }

    public LiveData<List<MailItem>> getTrashMails() {
        return mailDao.getTrashMailsLive();
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

    // --- SERVER ---

    public LiveData<List<MailItem>> fetchMailsFromServer(String token, String userId) {
        MutableLiveData<List<MailItem>> mailLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/api/mails/");
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

                Type listType = new TypeToken<List<MailItem>>() {}.getType();
                List<MailItem> mails = gson.fromJson(result.toString(), listType);
                mailLiveData.postValue(mails);

            } catch (Exception e) {
                Log.e("MailRepository", "fetchMailsFromServer error", e);
            }
        });
        return mailLiveData;
    }

    public void sendMailToServer(String token, String userId, MailItem mail) {
        executorService.execute(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/api/mails");
                HttpURLConnection conn = setupConnection(url, "POST", token, userId);

                String json = gson.toJson(mail);
                writeJson(conn, json);

                if (conn.getResponseCode() != 200) {
                    Log.e("MailRepository", "sendMail failed: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                Log.e("MailRepository", "sendMail error", e);
            }
        });
    }

    public void updateMailOnServer(String token, String userId, String mailId, MailItem updatedMail) {
        executorService.execute(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/api/mails/" + mailId);
                HttpURLConnection conn = setupConnection(url, "PATCH", token, userId);

                String json = gson.toJson(updatedMail);
                writeJson(conn, json);

                if (conn.getResponseCode() != 200) {
                    Log.e("MailRepository", "updateMail failed: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                Log.e("MailRepository", "updateMail error", e);
            }
        });
    }

    public void deleteMailOnServer(String token, String userId, String mailId) {
        executorService.execute(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/api/mails/" + mailId);
                HttpURLConnection conn = setupConnection(url, "PATCH", token, userId);

                String json = "{\"isDeleted\": true}";
                writeJson(conn, json);

                if (conn.getResponseCode() != 200) {
                    Log.e("MailRepository", "deleteMail failed: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                Log.e("MailRepository", "deleteMail error", e);
            }
        });
    }

    public void deleteMailPermanently(String token, String userId, String mailId) {
        executorService.execute(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/api/mails/" + mailId);
                HttpURLConnection conn = setupConnection(url, "DELETE", token, userId);
                conn.connect();

                if (conn.getResponseCode() != 200 && conn.getResponseCode() != 204) {
                    Log.e("MailRepository", "deletePermanently failed: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                Log.e("MailRepository", "deletePermanently error", e);
            }
        });
    }

    public void markMailAsSpam(String token, String userId, String mailId) {
        executorService.execute(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/api/mails/" + mailId);
                HttpURLConnection conn = setupConnection(url, "PATCH", token, userId);

                String json = "{\"isSpam\": true, \"isDeleted\": false}";
                writeJson(conn, json);

                if (conn.getResponseCode() != 200) {
                    Log.e("MailRepository", "markSpam failed: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                Log.e("MailRepository", "markSpam error", e);
            }
        });
    }

    public void unmarkMailAsSpam(String token, String userId, String mailId) {
        executorService.execute(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/api/mails/" + mailId);
                HttpURLConnection conn = setupConnection(url, "PATCH", token, userId);

                String json = "{\"isSpam\": false}";
                writeJson(conn, json);

                if (conn.getResponseCode() != 200) {
                    Log.e("MailRepository", "unmarkSpam failed: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                Log.e("MailRepository", "unmarkSpam error", e);
            }
        });
    }

    public void toggleStarForMail(String token, String userId, String mailId, boolean isStarred) {
        executorService.execute(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/api/mails/" + mailId);
                HttpURLConnection conn = setupConnection(url, "PATCH", token, userId);

                String json = "{\"isStarred\": " + isStarred + "}";
                writeJson(conn, json);

                if (conn.getResponseCode() != 200) {
                    Log.e("MailRepository", "toggleStar failed: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                Log.e("MailRepository", "toggleStar error", e);
            }
        });
    }

    // --- Utility methods ---

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
        OutputStream os = conn.getOutputStream();
        os.write(json.getBytes());
        os.flush();
        os.close();
    }
}
