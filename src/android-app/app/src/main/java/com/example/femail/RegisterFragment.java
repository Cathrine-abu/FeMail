package com.example.femail;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.concurrent.Executors;

public class RegisterFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private ImageView imageViewProfilePreview;
    private EditText editTextUsername, editTextPassword, editTextConfirmPassword, editTextFullName, editTextPhone, editTextBirthDate;
    private Spinner spinnerGender;
    private Button buttonChooseImage, buttonRegisterSubmit;
    private Uri imageUri;
    private String base64Image;
    private Bitmap cameraBitmap;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        editTextUsername = view.findViewById(R.id.editTextRegisterUsername);
        editTextPassword = view.findViewById(R.id.editTextRegisterPassword);
        editTextConfirmPassword = view.findViewById(R.id.editTextRegisterConfirmPassword);
        editTextFullName = view.findViewById(R.id.editTextRegisterFullName);
        editTextPhone = view.findViewById(R.id.editTextRegisterPhone);
        editTextBirthDate = view.findViewById(R.id.editTextRegisterBirthDate);
        spinnerGender = view.findViewById(R.id.spinnerRegisterGender);
        buttonChooseImage = view.findViewById(R.id.buttonChooseImage);
        imageViewProfilePreview = view.findViewById(R.id.imageViewProfilePreview);
        buttonRegisterSubmit = view.findViewById(R.id.buttonRegisterSubmit);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        editTextBirthDate.setOnClickListener(v -> showDatePicker());
        buttonChooseImage.setOnClickListener(v -> showImageSourceDialog());
        buttonRegisterSubmit.setOnClickListener(v -> handleRegister());

        // Register activity result launchers
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap bitmap = (Bitmap) extras.get("data");
                        if (bitmap != null) {
                            cameraBitmap = bitmap;
                            imageViewProfilePreview.setImageBitmap(bitmap);
                            base64Image = encodeImage(bitmap);
                        }
                    }
                }
            }
        );
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                        imageViewProfilePreview.setImageBitmap(bitmap);
                        base64Image = encodeImage(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        );
        requestCameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    showToast("Camera permission denied");
                }
            }
        );

        // Test server connectivity when fragment loads
        testServerConnectivity();

        return view;
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {
            String date = String.format("%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
            editTextBirthDate.setText(date);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showImageSourceDialog() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Camera
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                    } else {
                        openCamera();
                    }
                } else {
                    // Gallery
                    openGallery();
                }
            })
            .show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] imageBytes = baos.toByteArray();
        return "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void handleRegister() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();
        String fullName = editTextFullName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String birthDate = editTextBirthDate.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString().toLowerCase();

        // Validation (same as before, but gender must be valid)
        if (!username.matches("^[a-z0-9]+$") || username.replaceAll("[^a-z]", "").length() < 3) {
            showToast(getString(R.string.register_error_username)); return;
        }
        if (password.length() < 8 || !password.matches(".*\\d.*") || !password.matches(".*[A-Za-z].*")) {
            showToast(getString(R.string.register_error_password)); return;
        }
        if (!password.equals(confirmPassword)) {
            showToast(getString(R.string.register_error_confirm_password)); return;
        }
        if (TextUtils.isEmpty(fullName)) {
            showToast(getString(R.string.register_error_full_name)); return;
        }
        if (!phone.matches("^\\d{10}$")) {
            showToast(getString(R.string.register_error_phone)); return;
        }
        if (TextUtils.isEmpty(birthDate)) {
            showToast(getString(R.string.register_error_birth_date)); return;
        } else {
            String[] parts = birthDate.split("-");
            int year = Integer.parseInt(parts[0]);
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - year;
            if (age < 14) {
                showToast(getString(R.string.register_error_birth_date)); return;
            }
        }
        // Improved gender validation
        if (gender.equals("select gender") || (!gender.equals("male") && !gender.equals("female") && !gender.equals("other"))) {
            showToast(getString(R.string.register_error_gender)); return;
        }
        if (base64Image == null) {
            showToast(getString(R.string.register_error_image)); return;
        }

        // API call in background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                android.util.Log.d("RegisterRequest", "Starting registration for user: " + username);
                android.util.Log.d("RegisterRequest", "Gender being sent: " + gender);
                
                URL url = new URL("http://10.0.2.2:8080/api/users");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000); // 30 seconds
                conn.setReadTimeout(30000); // 30 seconds

                JSONObject json = new JSONObject();
                json.put("username", username);
                json.put("password", password);
                json.put("full_name", fullName);
                json.put("phone", phone);
                json.put("birth_date", birthDate);
                json.put("gender", gender);
                json.put("image", base64Image);

                String jsonString = json.toString();
                android.util.Log.d("RegisterRequest", "Request JSON length: " + jsonString.length());
                android.util.Log.d("RegisterRequest", "Request JSON preview: " + jsonString.substring(0, Math.min(300, jsonString.length())) + "...");

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonString);
                os.flush();
                os.close();

                android.util.Log.d("RegisterRequest", "Request sent, waiting for response...");

                int responseCode = conn.getResponseCode();
                android.util.Log.d("RegisterRequest", "Response code: " + responseCode);
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
                    android.util.Log.d("RegisterResponse", "Response Code: " + responseCode);
                    android.util.Log.d("RegisterResponse", "Response Body: " + response.toString());
                    
                    if (responseCode >= 200 && responseCode < 300) {
                        showToast(getString(R.string.register_success));
                        NavHostFragment.findNavController(this).navigateUp();
                    } else {
                        String errorMsg = getString(R.string.register_failed);
                        try {
                            JSONObject resp = new JSONObject(response.toString());
                            if (resp.has("error")) {
                                errorMsg = resp.getString("error");
                                android.util.Log.d("RegisterError", "Server error: " + errorMsg);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("RegisterError", "Failed to parse error response", e);
                        }
                        showToast(errorMsg);
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("RegisterError", "Network error during registration", e);
                getActivity().runOnUiThread(() -> showToast(getString(R.string.register_failed) + ": " + e.getMessage()));
            }
        });
    }

    private void testServerConnectivity() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/api/users");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                
                int responseCode = conn.getResponseCode();
                android.util.Log.d("ConnectivityTest", "Server responded with code: " + responseCode);
                
                getActivity().runOnUiThread(() -> {
                    if (responseCode >= 200 && responseCode < 500) {
                        showToast("Server is reachable (HTTP " + responseCode + ")");
                    } else {
                        showToast("Server returned error: " + responseCode);
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("ConnectivityTest", "Failed to connect to server", e);
                getActivity().runOnUiThread(() -> showToast("Cannot connect to server: " + e.getMessage()));
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}