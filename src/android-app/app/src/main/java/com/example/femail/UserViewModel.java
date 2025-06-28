package com.example.femail;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class UserViewModel extends AndroidViewModel {
    private final UserRepository repository;
    public final MutableLiveData<AuthResult> loginResult = new MutableLiveData<>();
    public final MutableLiveData<AuthResult> registerResult = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository();
    }

    public void login(String username, String password) {
        repository.login(username, password, (success, message) -> {
            loginResult.setValue(new AuthResult(success, message));
        });
    }

    public void register(String username, String password, String fullName, String phone, String birthDate, String gender, String base64Image) {
        repository.register(username, password, fullName, phone, birthDate, gender, base64Image, (success, message) -> {
            registerResult.setValue(new AuthResult(success, message));
        });
    }

    public static class AuthResult {
        public final boolean success;
        public final String message;
        public AuthResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
} 