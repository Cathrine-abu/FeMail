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
import androidx.lifecycle.ViewModelProvider;
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

    private UserViewModel userViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
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

        userViewModel.registerResult.observe(getViewLifecycleOwner(), result -> {
            buttonRegisterSubmit.setEnabled(true);
            buttonRegisterSubmit.setText("Register");
            if (result.success) {
                AuthPrefs.saveAuthData(requireContext(), null, result.userId, result.username);
                showToast("Registration successful! Please log in.");
                NavHostFragment.findNavController(RegisterFragment.this)
                        .navigateUp();
            } else {
                showToast(result.message);
            }
        });

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

        // Validation (same as before)
        if (!username.matches("^[a-z0-9]+$") || username.replaceAll("[^a-z]", "").length() < 3) {
            showToast("Invalid username"); return;
        }
        if (password.length() < 8 || !password.matches(".*\\d.*") || !password.matches(".*[A-Za-z].*")) {
            showToast("Password must be at least 8 characters and contain letters and numbers"); return;
        }
        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match"); return;
        }
        if (fullName.isEmpty()) {
            showToast("Full name is required"); return;
        }
        if (!phone.matches("^\\d{10}$")) {
            showToast("Phone must be 10 digits"); return;
        }

        buttonRegisterSubmit.setEnabled(false);
        buttonRegisterSubmit.setText("Registering...");
        userViewModel.register(username, password, fullName, phone, birthDate, gender, base64Image);
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
                
            } catch (Exception e) {
                android.util.Log.e("ConnectivityTest", "Failed to connect to server", e);
            }
        });
    }

    private void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
}