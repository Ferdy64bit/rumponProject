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
 * RegisterViewModel - ViewModel for Register screen.
 */
public class RegisterViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final SessionManager sessionManager;
    private final MutableLiveData<AuthRepository.AuthRepoResult> authResult = new MutableLiveData<>();

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository();
        this.sessionManager = new SessionManager(application);
    }

    public LiveData<AuthRepository.AuthRepoResult> getAuthResult() {
        return authResult;
    }

    public void register(String fullName, String email, String password, String confirmPassword) {
        if (validateInput(fullName, email, password, confirmPassword)) {
            authRepository.register(fullName, email, password, authResult);
        }
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

    private boolean validateInput(String fullName, String email, String password, String confirmPassword) {
        if (fullName == null || fullName.isEmpty()) {
            authResult.setValue(AuthRepository.AuthRepoResult.error("Nama lengkap tidak boleh kosong."));
            return false;
        }
        if (email == null || email.isEmpty()) {
            authResult.setValue(AuthRepository.AuthRepoResult.error("Email tidak boleh kosong."));
            return false;
        }
        if (password == null || password.isEmpty()) {
            authResult.setValue(AuthRepository.AuthRepoResult.error("Password tidak boleh kosong."));
            return false;
        }
        if (password.length() < 6) {
            authResult.setValue(AuthRepository.AuthRepoResult.error("Password minimal 6 karakter."));
            return false;
        }
        if (!password.equals(confirmPassword)) {
            authResult.setValue(AuthRepository.AuthRepoResult.error("Konfirmasi password tidak cocok."));
            return false;
        }
        return true;
    }
}
