package com.example.femail;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UserRepository() {
        // No initialization needed for network-only repository
    }

    public interface AuthCallback {
        void onResult(boolean success, String message);
    }

    public void login(String username, String password, AuthCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            String errorMsg = "Unknown error";
            try {
                URL url = new URL("http://10.0.2.2:8080/api/tokens");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("username", username);
                json.put("password", password);

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(json.toString());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream()
                ));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                if (responseCode >= 200 && responseCode < 300) {
                    success = true;
                } else {
                    JSONObject resp = new JSONObject(response.toString());
                    errorMsg = resp.has("error") ? resp.getString("error") : "Invalid username or password";
                }
            } catch (Exception e) {
                errorMsg = "Network error: " + e.getMessage();
            }
            final boolean finalSuccess = success;
            final String finalErrorMsg = errorMsg;
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(finalSuccess, finalErrorMsg));
        });
    }

    public void register(String username, String password, String fullName, String phone, String birthDate, String gender, String base64Image, AuthCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            String errorMsg = "Unknown error";
            try {
                URL url = new URL("http://10.0.2.2:8080/api/users");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("username", username);
                json.put("password", password);
                json.put("full_name", fullName);
                json.put("phone", phone);
                json.put("birth_date", birthDate);
                json.put("gender", gender);
                json.put("image", base64Image);

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(json.toString());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream()
                ));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                if (responseCode >= 200 && responseCode < 300) {
                    success = true;
                } else {
                    JSONObject resp = new JSONObject(response.toString());
                    errorMsg = resp.has("error") ? resp.getString("error") : "Registration failed";
                }
            } catch (Exception e) {
                errorMsg = "Network error: " + e.getMessage();
            }
            final boolean finalSuccess = success;
            final String finalErrorMsg = errorMsg;
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(finalSuccess, finalErrorMsg));
        });
    }
} 