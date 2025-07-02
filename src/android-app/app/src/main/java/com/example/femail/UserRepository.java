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
        void onResult(boolean success, String message, String token, String userId, String username);
    }

    public void login(String username, String password, AuthCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            String errorMsg = "Unknown error";
            String token = null;
            String userId = null;
            String returnedUsername = null;
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
                    JSONObject resp = new JSONObject(response.toString());
                    token = resp.optString("token", null);
                    success = token != null;
                    
                    if (success) {
                        // Try to get userId and username from response first
                        userId = resp.optString("userId", null);
                        returnedUsername = resp.optString("username", null);
                        
                        // If not in response, try to decode from JWT token
                        if (userId == null || returnedUsername == null) {
                            try {
                                // Decode JWT token to get user info
                                String[] parts = token.split("\\.");
                                if (parts.length == 3) {
                                    String payload = parts[1];
                                    // Add padding if needed
                                    while (payload.length() % 4 != 0) {
                                        payload += "=";
                                    }
                                    // Replace URL-safe characters
                                    payload = payload.replace('-', '+').replace('_', '/');
                                    
                                    // Decode base64
                                    byte[] decodedBytes = android.util.Base64.decode(payload, android.util.Base64.DEFAULT);
                                    String decodedPayload = new String(decodedBytes);
                                    JSONObject tokenPayload = new JSONObject(decodedPayload);
                                    
                                    if (userId == null) {
                                        userId = tokenPayload.optString("user_id", null);
                                    }
                                    if (returnedUsername == null) {
                                        returnedUsername = tokenPayload.optString("username", null);
                                    }
                                }
                            } catch (Exception e) {
                                // If JWT decoding fails, use the provided username
                                returnedUsername = username;
                            }
                        }
                    }
                    
                    if (!success) errorMsg = "Invalid server response";
                } else {
                    JSONObject resp = new JSONObject(response.toString());
                    errorMsg = resp.has("error") ? resp.getString("error") : "Invalid username or password";
                }
            } catch (Exception e) {
                errorMsg = "Network error: " + e.getMessage();
            }
            final boolean finalSuccess = success;
            final String finalErrorMsg = errorMsg;
            final String finalToken = token;
            final String finalUserId = userId;
            final String finalUsername = returnedUsername;
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(finalSuccess, finalErrorMsg, finalToken, finalUserId, finalUsername));
        });
    }

    public void register(String username, String password, String fullName, String phone, String birthDate, String gender, String base64Image, AuthCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            String errorMsg = "Unknown error";
            String token = null;
            String userId = null;
            String returnedUsername = null;
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

                if (responseCode == 201) {
                    JSONObject resp = new JSONObject(response.toString());
                    JSONObject userObj = resp.optJSONObject("user");
                    if (userObj != null) {
                        returnedUsername = userObj.optString("username", null);
                        userId = userObj.has("id") ? String.valueOf(userObj.optInt("id")) : null;
                    }
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
            final String finalToken = null;
            final String finalUserId = userId;
            final String finalUsername = returnedUsername;
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(finalSuccess, finalErrorMsg, finalToken, finalUserId, finalUsername));
        });
    }
} 