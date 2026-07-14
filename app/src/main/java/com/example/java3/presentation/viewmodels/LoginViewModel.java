package com.example.java3.presentation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.java3.data.repository.AuthRepository;
import com.example.java3.core.utils.SessionManager;
import com.google.firebase.auth.AuthCredential;

/**
 * LoginViewModel - ViewModel for Login screen.
 */
public class LoginViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final SessionManager sessionManager;
    private final MutableLiveData<AuthRepository.AuthRepoResult> authResult = new MutableLiveData<>();
    private final MutableLiveData<String> resetPasswordResult = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository();
        this.sessionManager = new SessionManager(application);
    }

    public LiveData<AuthRepository.AuthRepoResult> getAuthResult() {
        return authResult;
    }

    public LiveData<String> getResetPasswordResult() {
        return resetPasswordResult;
    }

    public void login(String email, String password) {
        if (validateInput(email, password)) {
            authRepository.login(email, password, authResult);
        }
    }

    public void forgotPassword(String email) {
        if (email == null || email.isEmpty()) {
            resetPasswordResult.setValue("Masukkan email terlebih dahulu.");
            return;
        }
        authRepository.forgotPassword(email, resetPasswordResult);
    }

    public void signInWithGoogle(AuthCredential credential) {
        authRepository.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                authResult.setValue(AuthRepository.AuthRepoResult.success(task.getResult().getUser().getUid()));
            } else {
                authResult.setValue(AuthRepository.AuthRepoResult.error(task.getException() != null ? task.getException().getMessage() : "Google Sign-In failed"));
            }
        });
    }

    public void saveSession(String uid, String email) {
        sessionManager.saveSession(uid, email);
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public String getUserUid() {
        return sessionManager.getUserUid();
    }

    public String getUserEmail() {
        return sessionManager.getUserEmail();
    }

    private boolean validateInput(String email, String password) {
        if (email == null || email.isEmpty()) {
            authResult.setValue(AuthRepository.AuthRepoResult.error("Email tidak boleh kosong."));
            return false;
        }
        if (password == null || password.isEmpty()) {
            authResult.setValue(AuthRepository.AuthRepoResult.error("Password tidak boleh kosong."));
            return false;
        }
        return true;
    }
}
