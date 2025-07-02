package com.example.femail.Mails;

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
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    public MailRepository(Application application) {
        MailDatabase db = MailDatabase.getDatabase(application);
        mailDao = db.mailDao();
    }

    // --- ROOM ---
    public LiveData<List<MailItem>> getAllMails(String userId) {
        return mailDao.getAllMails(userId);
    }

    public LiveData<List<MailItem>> getInboxMails(String userId) {
        return mailDao.getInboxMailsLive(userId);
    }

    public LiveData<List<MailItem>> getSentMails(String userId) {
        return mailDao.getSentMailsLive(userId);
    }

    public LiveData<List<MailItem>> getDraftMails(String userId) {
        return mailDao.getDraftMailsLive(userId);
    }

    public LiveData<List<MailItem>> getSpamMails(String userId) {
        return mailDao.getSpamMailsLive(userId);
    }

    public LiveData<List<MailItem>> getStarredMails(String userId) {
        return mailDao.getStarredMailsLive(userId);
    }

    public LiveData<List<MailItem>> getTrashMails(String userId) {
        return mailDao.getTrashMailsLive(userId);
    }

    public LiveData<List<MailItem>> getPrimaryMails(String userId) {
        return mailDao.getPrimaryMailsLive(userId);
    }

    public LiveData<List<MailItem>> getSocialMails(String userId) {
        return mailDao.getSocialMailsLive(userId);
    }

    public LiveData<List<MailItem>> getPromotionsMails(String userId) {
        return mailDao.getPromotionsMailsLive(userId);
    }

    public LiveData<List<MailItem>> getUpdatesMails(String userId) {
        return mailDao.getUpdatesMailsLive(userId);
    }

    public LiveData<List<MailItem>> getMailsByLabel(String labelName, String userId) {
        return mailDao.getMailsByLabel(labelName, userId);
    }

    public LiveData<List<MailItem>> searchMails(String query, String userId) {
        return mailDao.searchMails(query, userId);
    }

    public void insert(MailItem mail) {
        executorService.execute(() -> mailDao.insertMail(mail));
    }

    public void delete(MailItem mail) {
        executorService.execute(() -> mailDao.deleteMail(mail));
    }

    public void update(MailItem mail) {
        executorService.execute(() -> mailDao.updateMail(mail));
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