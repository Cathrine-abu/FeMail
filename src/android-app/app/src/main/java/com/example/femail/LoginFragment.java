package com.example.femail;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.navigation.fragment.NavHostFragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import androidx.core.content.ContextCompat;

public class LoginFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        EditText usernameEditText = view.findViewById(R.id.editTextUsername);
        EditText passwordEditText = view.findViewById(R.id.editTextPassword);
        Button loginButton = view.findViewById(R.id.buttonLogin);
        TextView registerPrompt = view.findViewById(R.id.textViewRegisterPrompt);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();
                
                // Validation
                if (username.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Disable button during login
                loginButton.setEnabled(false);
                loginButton.setText("Logging in...");
                
                // API call in background thread
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        android.util.Log.d("LoginRequest", "Starting login for user: " + username);
                        
                        URL url = new URL("http://10.0.2.2:8080/api/tokens");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        conn.setDoOutput(true);
                        conn.setConnectTimeout(30000);
                        conn.setReadTimeout(30000);

                        JSONObject json = new JSONObject();
                        json.put("username", username);
                        json.put("password", password);

                        android.util.Log.d("LoginRequest", "Request JSON: " + json.toString());

                        DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                        os.writeBytes(json.toString());
                        os.flush();
                        os.close();

                        int responseCode = conn.getResponseCode();
                        android.util.Log.d("LoginRequest", "Response code: " + responseCode);
                        
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream()
                        ));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        getActivity().runOnUiThread(() -> {
                            // Re-enable button
                            loginButton.setEnabled(true);
                            loginButton.setText("Login");
                            
                            android.util.Log.d("LoginResponse", "Response Code: " + responseCode);
                            android.util.Log.d("LoginResponse", "Response Body: " + response.toString());
                            
                            if (responseCode >= 200 && responseCode < 300) {
                                try {
                                    JSONObject resp = new JSONObject(response.toString());
                                    String token = resp.getString("token");
                                    android.util.Log.d("LoginSuccess", "Login successful, token received");
                                    
                                    storeAuthToken(token);
                                    
                                    Toast.makeText(getActivity(), "Login successful!", Toast.LENGTH_SHORT).show();
                                    NavHostFragment.findNavController(LoginFragment.this)
                                        .navigate(R.id.action_LoginFragment_to_FirstFragment);
                                } catch (Exception e) {
                                    android.util.Log.e("LoginError", "Failed to parse login response", e);
                                    Toast.makeText(getActivity(), "Login failed: Invalid response", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                String errorMsg = getString(R.string.login_error);
                                try {
                                    JSONObject resp = new JSONObject(response.toString());
                                    if (resp.has("error")) {
                                        errorMsg = resp.getString("error");
                                        android.util.Log.d("LoginError", "Server error: " + errorMsg);
                                    }
                                } catch (Exception e) {
                                    android.util.Log.e("LoginError", "Failed to parse error response", e);
                                }
                                Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        android.util.Log.e("LoginError", "Network error during login", e);
                        getActivity().runOnUiThread(() -> {
                            // Re-enable button
                            loginButton.setEnabled(true);
                            loginButton.setText("Login");
                            Toast.makeText(getActivity(), "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        });

        String promptText = "Don't have an account? Register here.";
        SpannableString spannable = new SpannableString(promptText);
        int start = promptText.indexOf("Register here");
        int end = start + "Register here".length();
        // Set 'Register here' to accent color
        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.accent)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Set the rest to black
        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), android.R.color.black)), 0, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_LoginFragment_to_RegisterFragment);
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        registerPrompt.setText(spannable);
        registerPrompt.setMovementMethod(LinkMovementMethod.getInstance());

        // Test server connectivity when fragment loads
        testServerConnectivity();

        return view;
    }

    private void storeAuthToken(String token) {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("FeMail", android.content.Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putString("auth_token", token);
        editor.putLong("token_timestamp", System.currentTimeMillis());
        editor.apply();
        android.util.Log.d("LoginSuccess", "Token stored in SharedPreferences");
    }

    private void testServerConnectivity() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/api/tokens");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                android.util.Log.d("ServerConnectivity", "Response code: " + responseCode);

                getActivity().runOnUiThread(() -> {
                    if (responseCode == 200) {
                        Toast.makeText(getActivity(), "Server is reachable", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Server is not reachable, response code: " + responseCode, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("ServerConnectivity", "Error checking server connectivity", e);
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Error checking server connectivity: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}