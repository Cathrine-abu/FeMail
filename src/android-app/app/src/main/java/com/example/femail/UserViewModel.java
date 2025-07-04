package com.example.femail;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class UserViewModel extends AndroidViewModel {
    private final UserRepository repository;
    public final MutableLiveData<AuthResult> loginResult = new MutableLiveData<>();
    public final MutableLiveData<AuthResult> registerResult = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public void login(String username, String password) {
        repository.login(username, password, (success, message, token, userId, returnedUsername) -> {
            loginResult.setValue(new AuthResult(success, message, token, userId, returnedUsername));
        });
    }

    public void register(String username, String password, String fullName, String phone, String birthDate, String gender, String base64Image) {
        repository.register(username, password, fullName, phone, birthDate, gender, base64Image, (success, message, token, userId, returnedUsername) -> {
            registerResult.setValue(new AuthResult(success, message, token, userId, returnedUsername));
        });
    }

    public LiveData<User> getUserFromServer(String token, String userId) {
        return repository.getUserFromServer(token, userId);
    }

    public static class AuthResult {
        public final boolean success;
        public final String message;
        public final String token;
        public final String userId;
        public final String username;
        public AuthResult(boolean success, String message, String token, String userId, String username) {
            this.success = success;
            this.message = message;
            this.token = token;
            this.userId = userId;
            this.username = username;
        }
    }
} 