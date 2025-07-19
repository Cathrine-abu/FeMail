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
import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;

public class LoginFragment extends Fragment {

    private UserViewModel userViewModel;
    private EditText usernameEditText, passwordEditText;
    private Button loginButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        usernameEditText = view.findViewById(R.id.editTextUsername);
        passwordEditText = view.findViewById(R.id.editTextPassword);
        loginButton = view.findViewById(R.id.buttonLogin);
        TextView registerPrompt = view.findViewById(R.id.textViewRegisterPrompt);

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            if (username.isEmpty()) {
                usernameEditText.setError(getString(R.string.error_username_required));
                return;
            }
            if (password.isEmpty()) {
                passwordEditText.setError(getString(R.string.error_password_required));
                return;
            }
            if (password.length() < 8) {
                passwordEditText.setError(getString(R.string.error_password_too_short));
                return;
            }
            loginButton.setEnabled(false);
            loginButton.setText("Logging in...");
            userViewModel.login(username, password);
        });

        userViewModel.loginResult.observe(getViewLifecycleOwner(), result -> {
            loginButton.setEnabled(true);
            loginButton.setText("Login");
            if (result.success) {
                // Debug logging
                android.util.Log.d("LoginFragment", "Login successful - Token: " + (result.token != null ? "present" : "null") + 
                    ", UserId: " + result.userId + ", Username: " + result.username);
                
                AuthPrefs.saveAuthData(requireContext(), result.token, result.userId, result.username);
                
                // Verify saved data
                String savedToken = AuthPrefs.getToken(requireContext());
                String savedUserId = AuthPrefs.getUserId(requireContext());
                String savedUsername = AuthPrefs.getUsername(requireContext());
                android.util.Log.d("LoginFragment", "Saved data - Token: " + (savedToken != null ? "present" : "null") + 
                    ", UserId: " + savedUserId + ", Username: " + savedUsername);
                
                Toast.makeText(getActivity(), "Login successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), MailActivity.class);
                startActivity(intent);
                requireActivity().finish();
            } else {
                Toast.makeText(getActivity(), result.message, Toast.LENGTH_SHORT).show();
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

                // Removed all Toasts here
            } catch (Exception e) {
                android.util.Log.e("ServerConnectivity", "Error checking server connectivity", e);
                // Removed Toast here too
            }
        });
    }
}