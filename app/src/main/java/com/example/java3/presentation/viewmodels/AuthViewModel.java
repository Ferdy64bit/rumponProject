package com.example.java3.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.java3.data.repository.AuthRepository;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> resetPasswordSuccessLiveData = new MutableLiveData<>();

    public AuthViewModel() {
        repository = new AuthRepository();
        if (repository.getCurrentUser() != null) {
            userLiveData.setValue(repository.getCurrentUser());
        }
    }

    public LiveData<FirebaseUser> getUserLiveData() { return userLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }
    public LiveData<String> getResetPasswordSuccessLiveData() { return resetPasswordSuccessLiveData; }

    public void login(String email, String password) {
        loadingLiveData.setValue(true);
        repository.login(email, password)
            .addOnCompleteListener(task -> {
                loadingLiveData.setValue(false);
                if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                    userLiveData.setValue(repository.getCurrentUser());
                } else {
                    handleError(task.getException(), "Login failed");
                }
            });
    }

    public void register(String email, String password, String fullName) {
        loadingLiveData.setValue(true);
        repository.register(email, password, fullName)
            .addOnCompleteListener(task -> {
                loadingLiveData.setValue(false);
                if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                    userLiveData.setValue(repository.getCurrentUser());
                } else {
                    handleError(task.getException(), "Registration failed");
                }
            });
    }

    public void signInWithGoogle(AuthCredential credential) {
        loadingLiveData.setValue(true);
        repository.signInWithCredential(credential)
            .addOnCompleteListener(task -> {
                loadingLiveData.setValue(false);
                if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                    userLiveData.setValue(repository.getCurrentUser());
                } else {
                    handleError(task.getException(), "Google Sign-In failed");
                }
            });
    }

    public void resetPassword(String email) {
        loadingLiveData.setValue(true);
        repository.resetPassword(email)
            .addOnCompleteListener(task -> {
                loadingLiveData.setValue(false);
                if (task.isSuccessful()) {
                    resetPasswordSuccessLiveData.setValue("Password reset link sent to your email.");
                } else {
                    handleError(task.getException(), "Password reset failed");
                }
            });
    }

    public void logout() {
        repository.logout();
        userLiveData.setValue(null);
    }

    private void handleError(Exception e, String defaultMessage) {
        String message = e != null ? e.getMessage() : defaultMessage;
        errorLiveData.setValue(message);
    }
}
