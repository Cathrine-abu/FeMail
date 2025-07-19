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
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


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
        android.util.Log.d("MailRepository", "Getting draft mails for userId: " + userId);
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

    public void insert(MailItem mail, String token, String userId) {
        // Save to local database immediately for instant UI feedback
        executorService.execute(() -> {
            mail.setUserId(userId);
            android.util.Log.d("MailRepository", "Inserting draft to local DB: id=" + mail.id + ", subject=" + mail.subject + ", isDraft=" + mail.isDraft + ", direction=" + mail.direction);
            mailDao.insertMail(mail);
        });
        // Also send to server
        sendMailToServer(token, userId, mail);
    }

    public void delete(MailItem mail, String token, String userId) {
        deleteMailOnServer(token, userId, mail.id);
        executorService.execute(() -> mailDao.deleteMail(mail));
    }

    public void update(MailItem mail, String token, String userId) {
        updateMailOnServer(token, userId, mail, success -> {
            if (success) {
                executorService.execute(() -> {
                    mailDao.updateMail(mail);
                    mail.setUserId(userId + " ");
                    mail.setUserId(userId);
                    mailDao.updateMail(mail);
                });
            }
        });
    }


    public void deleteAll() {
        executorService.execute(mailDao::deleteAll);
    }

    // --- SERVER ---

    public LiveData<List<MailItem>> fetchMailsFromServer(String token, String userId) {
        return fetchMailsFromServer(token, userId, null);
    }

    public LiveData<List<MailItem>> fetchMailsFromServer(String token, String userId, String sinceTimestamp) {
        MutableLiveData<List<MailItem>> mailLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            try {
                String urlString = "http://10.0.2.2:8080/api/mails/";
                if (sinceTimestamp != null) {
                    urlString += "?since=" + sinceTimestamp;
                }
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("user-id", userId);
                conn.setRequestMethod("GET");
                conn.connect();

                int responseCode = conn.getResponseCode();
                Log.d("MailRepository", "fetchMailsFromServer response code: " + responseCode);

                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    Log.d("MailRepository", "fetchMailsFromServer response: " + result.toString());

                    Type listType = new TypeToken<List<MailItem>>() {}.getType();
                    List<MailItem> mails = gson.fromJson(result.toString(), listType);
                    // Set userId for each mail and convert timestamp to time
                    if (mails != null) {
                        for (MailItem mail : mails) {
                            mail.setUserId(userId);
                            mail.convertTimestampToTime(); // Convert timestamp to time for proper sorting
                        }
                        Log.d("MailRepository", "fetchMailsFromServer fetched " + mails.size() + " mails");
                        List<MailItem> allLocalMails = mailDao.getAllMails(userId).getValue();
                        for (MailItem serverMail : mails) {
                            // Delete any temp mail with matching subject/body
                            if (allLocalMails != null) {
                                for (MailItem localMail : allLocalMails) {
                                    if (localMail.id.startsWith("temp-")
                                        && localMail.subject.equals(serverMail.subject)
                                        && localMail.body.equals(serverMail.body)) {
                                        mailDao.deleteMail(localMail);
                                    }
                                }
                            }
                            mailDao.insertMail(serverMail);
                        }
                    }
                    mailLiveData.postValue(mails);
                } else {
                    Log.e("MailRepository", "fetchMailsFromServer failed with response code: " + responseCode);
                    mailLiveData.postValue(null);
                }

            } catch (Exception e) {
                Log.e("MailRepository", "fetchMailsFromServer error", e);
                mailLiveData.postValue(null);
            }
        });
        return mailLiveData;
    }

    // New method to fetch only new mails since last sync
    public LiveData<List<MailItem>> fetchNewMailsFromServer(String token, String userId) {
        MutableLiveData<List<MailItem>> mailLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            try {
                // Get the most recent mail timestamp from local database
                MailItem latestMail = mailDao.getLatestMail(userId);
                String sinceTimestamp = null;
                if (latestMail != null && latestMail.time != null) {
                    // Convert local time format back to ISO timestamp for server
                    try {
                        java.text.SimpleDateFormat localFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                        java.util.Date date = localFormat.parse(latestMail.time);
                        java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                        sinceTimestamp = isoFormat.format(date);
                    } catch (Exception e) {
                        Log.e("MailRepository", "Error parsing timestamp", e);
                    }
                }
                
                // Fetch new mails from server
                String urlString = "http://10.0.2.2:8080/api/mails/";
                if (sinceTimestamp != null) {
                    urlString += "?since=" + sinceTimestamp;
                }
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("user-id", userId);
                conn.setRequestMethod("GET");
                conn.connect();

                int responseCode = conn.getResponseCode();
                Log.d("MailRepository", "fetchNewMailsFromServer response code: " + responseCode);

                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    Type listType = new TypeToken<List<MailItem>>() {}.getType();
                    List<MailItem> mails = gson.fromJson(result.toString(), listType);
                    
                    if (mails != null && !mails.isEmpty()) {
                        // Set userId for each mail and convert timestamp to time
                        for (MailItem mail : mails) {
                            mail.setUserId(userId);
                            mail.convertTimestampToTime();
                        }
                        
                        // Insert new mails into local database
                        for (MailItem mail : mails) {
                            mailDao.insertMail(mail);
                        }
                        
                        Log.d("MailRepository", "fetchNewMailsFromServer inserted " + mails.size() + " new mails");
                        mailLiveData.postValue(mails);
                    } else {
                        Log.d("MailRepository", "fetchNewMailsFromServer no new mails found");
                        mailLiveData.postValue(new ArrayList<>());
                    }
                } else {
                    Log.e("MailRepository", "fetchNewMailsFromServer failed with response code: " + responseCode);
                    mailLiveData.postValue(new ArrayList<>());
                }

            } catch (Exception e) {
                Log.e("MailRepository", "fetchNewMailsFromServer error", e);
                mailLiveData.postValue(new ArrayList<>());
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

                int responseCode = conn.getResponseCode();
                if (responseCode == 200 || responseCode == 201) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    // Parse the response from the server
                    try {
                        // Try to parse as full mail object first
                        MailItem serverMail = gson.fromJson(result.toString(), MailItem.class);
                        if (serverMail != null && serverMail.id != null) {
                            executorService.execute(() -> {
                                // Delete the temp mail first
                                if (mail.id.startsWith("temp-")) {
                                    mailDao.deleteMail(mail);
                                }
                                
                                // Convert timestamp to time format and insert the server mail with full data
                                serverMail.setUserId(userId);
                                serverMail.convertTimestampToTime();
                                mailDao.insertMail(serverMail);
                                
                                android.util.Log.d("MailRepository", "Successfully replaced temp mail with backend mail: id=" + serverMail.id + ", subject=" + serverMail.subject + ", isDraft=" + serverMail.isDraft);
                                
                                // Debug: Check what's in the database after insertion
                                List<MailItem> allMails = mailDao.getAllMailsDebug(userId);
                                android.util.Log.d("MailRepository", "After server mail insertion - total mails: " + (allMails != null ? allMails.size() : 0));
                                if (allMails != null) {
                                    for (MailItem dbMail : allMails) {
                                        if (dbMail.id.equals(serverMail.id)) {
                                            android.util.Log.d("MailRepository", "Found server mail in DB: id=" + dbMail.id + ", isDraft=" + dbMail.isDraft + ", direction=" + dbMail.direction + ", userId=" + dbMail.userId);
                                            break;
                                        }
                                    }
                                }
                                
                                // Debug: Check drafts specifically
                                List<MailItem> drafts = mailDao.getDraftsDebug(userId);
                                android.util.Log.d("MailRepository", "Drafts after server mail insertion: " + (drafts != null ? drafts.size() : 0));
                                if (drafts != null) {
                                    for (MailItem draft : drafts) {
                                        android.util.Log.d("MailRepository", "Draft: id=" + draft.id + ", isDraft=" + draft.isDraft + ", direction=" + draft.direction);
                                    }
                                }
                            });
                        } else {
                            // Fallback to ID-only response
                            com.google.gson.JsonObject obj = gson.fromJson(result.toString(), com.google.gson.JsonObject.class);
                            if (obj.has("id")) {
                                String backendId = obj.get("id").getAsString();
                                if (backendId != null && !backendId.isEmpty() && !backendId.equals("undefined")) {
                                    executorService.execute(() -> {
                                        // Delete the temp mail first
                                        if (mail.id.startsWith("temp-")) {
                                            mailDao.deleteMail(mail);
                                        }
                                        
                                        // Update the mail with the backend ID and insert it
                                        mail.id = backendId;
                                        mail.setUserId(userId);
                                        mail.convertTimestampToTime(); // Convert timestamp to time format
                                        mailDao.insertMail(mail);
                                        
                                        android.util.Log.d("MailRepository", "Successfully replaced temp mail with backend mail: id=" + mail.id + ", subject=" + mail.subject + ", isDraft=" + mail.isDraft);
                                        
                                        // Debug: Check what's in the database after insertion
                                        List<MailItem> allMails = mailDao.getAllMailsDebug(userId);
                                        android.util.Log.d("MailRepository", "After fallback insertion - total mails: " + (allMails != null ? allMails.size() : 0));
                                        if (allMails != null) {
                                            for (MailItem dbMail : allMails) {
                                                if (dbMail.id.equals(mail.id)) {
                                                    android.util.Log.d("MailRepository", "Found fallback mail in DB: id=" + dbMail.id + ", isDraft=" + dbMail.isDraft + ", direction=" + dbMail.direction + ", userId=" + dbMail.userId);
                                                    break;
                                                }
                                            }
                                        }
                                        
                                        // Debug: Check drafts specifically
                                        List<MailItem> drafts = mailDao.getDraftsDebug(userId);
                                        android.util.Log.d("MailRepository", "Drafts after fallback insertion: " + (drafts != null ? drafts.size() : 0));
                                        if (drafts != null) {
                                            for (MailItem draft : drafts) {
                                                android.util.Log.d("MailRepository", "Draft: id=" + draft.id + ", isDraft=" + draft.isDraft + ", direction=" + draft.direction);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("MailRepository", "Error parsing server response", e);
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("MailRepository", "Error sending mail to server", e);
            }
        });
    }

    public void updateMailOnServer(String token, String userId, MailItem mail, java.util.function.Consumer<Boolean> callback) {
        executorService.execute(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/api/mails/" + mail.id);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("user-id", userId);
                conn.setDoOutput(true);

                String jsonBody = gson.toJson(mail);
                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    // Try to read the response to get the updated mail object
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        reader.close();

                        // Try to parse as full mail object
                        MailItem updatedMail = gson.fromJson(result.toString(), MailItem.class);
                        if (updatedMail != null && updatedMail.id != null) {
                            executorService.execute(() -> {
                                updatedMail.setUserId(userId);
                                updatedMail.convertTimestampToTime(); // Convert timestamp to time format
                                mailDao.insertMail(updatedMail); // This will update the existing mail
                                android.util.Log.d("MailRepository", "Successfully updated mail from server: id=" + updatedMail.id + ", subject=" + updatedMail.subject);
                            });
                        }
                    } catch (Exception e) {
                        android.util.Log.e("MailRepository", "Error parsing PATCH response", e);
                    }
                }
                callback.accept(responseCode == 200);
            } catch (Exception e) {
                Log.e("UpdateMail", "Error updating mail", e);
                callback.accept(false);
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
    private List<String> extractUrls(String text) {
        List<String> urls = new ArrayList<>();
        Pattern pattern = Pattern.compile("(https?:\\/\\/[^\\s]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            urls.add(matcher.group());
        }
        return urls;
    }

    public void markMailAsSpam(String token, String userId, MailItem mail) {
        executorService.execute(() -> {
            try {
                mail.isSpam = true;
                mail.isDeleted = false;
                URL url = new URL("http://10.0.2.2:8080/api/mails/" + mail.id);
                HttpURLConnection conn = setupConnection(url, "PATCH", token, userId);
                String json = gson.toJson(mail);
                writeJson(conn, json);

                if (conn.getResponseCode() != 200) {
                    Log.e("MailRepository", "markMailAsSpam failed: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                Log.e("MailRepository", "markMailAsSpam error", e);
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

    public LiveData<MailItem> getMailById(String mailId) {
        MutableLiveData<MailItem> mailLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            MailItem mail = mailDao.getMailById(mailId);
            mailLiveData.postValue(mail);
        });
        return mailLiveData;
    }

    // Debug method to check all mails in database
    public void debugAllMails(String userId) {
        executorService.execute(() -> {
            List<MailItem> allMails = mailDao.getAllMailsDebug(userId);
            android.util.Log.d("MailRepository", "All mails in DB for user " + userId + ": " + (allMails != null ? allMails.size() : 0));
            if (allMails != null) {
                for (MailItem mail : allMails) {
                    android.util.Log.d("MailRepository", "Mail: id=" + mail.id + ", subject=" + mail.subject + ", isDraft=" + mail.isDraft + ", isDeleted=" + mail.isDeleted + ", direction=" + mail.direction);
                }
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