package com.example.java3.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.java3.core.managers.FirebaseManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

/**
 * AuthRepository - Handles data operations for Authentication.
 */
public class AuthRepository {
    private final FirebaseManager firebaseManager;

    public AuthRepository() {
        this.firebaseManager = new FirebaseManager();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseManager.getCurrentUser();
    }

    public Task<AuthResult> login(String email, String password) {
        return firebaseManager.login(email, password);
    }

    public Task<AuthResult> register(String email, String password, String fullName) {
        TaskCompletionSource<AuthResult> tcs = new TaskCompletionSource<>();
        firebaseManager.register(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                String uid = task.getResult().getUser().getUid();
                firebaseManager.createUserDocument(uid, fullName, email, "", "", new FirebaseManager.FirestoreCallback() {
                    @Override
                    public void onSuccess() {
                        sendVerificationEmail();
                        tcs.setResult(task.getResult());
                    }

                    @Override
                    public void onFailure(String error) {
                        tcs.setException(new Exception(error));
                    }
                });
            } else {
                tcs.setException(task.getException() != null ? task.getException() : new Exception("Registration failed"));
            }
        });
        return tcs.getTask();
    }

    public Task<Void> resetPassword(String email) {
        return firebaseManager.resetPassword(email);
    }

    public Task<AuthResult> signInWithCredential(AuthCredential credential) {
        TaskCompletionSource<AuthResult> tcs = new TaskCompletionSource<>();
        firebaseManager.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                FirebaseUser user = task.getResult().getUser();
                if (user != null) {
                    firebaseManager.checkUserExists(user.getUid(), exists -> {
                        if (!exists) {
                            firebaseManager.createUserDocument(user.getUid(), user.getDisplayName(),
                                    user.getEmail(), user.getPhoneNumber(),
                                    user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "",
                                    new FirebaseManager.FirestoreCallback() {
                                        @Override
                                        public void onSuccess() {
                                            tcs.setResult(task.getResult());
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            tcs.setException(new Exception(error));
                                        }
                                    });
                        } else {
                            firebaseManager.updateLastLogin(user.getUid());
                            tcs.setResult(task.getResult());
                        }
                    });
                } else {
                    tcs.setResult(task.getResult());
                }
            } else {
                tcs.setException(task.getException());
            }
        });
        return tcs.getTask();
    }

    public void login(String email, String password, MutableLiveData<AuthRepoResult> result) {
        login(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                result.setValue(AuthRepoResult.success(task.getResult().getUser().getUid()));
            } else {
                result.setValue(AuthRepoResult.error(handleException(task.getException())));
            }
        });
    }

    public void register(String fullName, String email, String password, MutableLiveData<AuthRepoResult> result) {
        register(email, password, fullName).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                result.setValue(AuthRepoResult.success(task.getResult().getUser().getUid()));
            } else {
                result.setValue(AuthRepoResult.error(handleException(task.getException())));
            }
        });
    }

    public void forgotPassword(String email, MutableLiveData<String> result) {
        resetPassword(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                result.setValue("Email reset password telah dikirim.");
            } else {
                result.setValue(handleException(task.getException()));
            }
        });
    }

    public void sendVerificationEmail() {
        firebaseManager.sendVerificationEmail();
    }

    public void logout() {
        firebaseManager.logout();
    }

    private String handleException(Exception e) {
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "Password terlalu lemah.";
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Email atau password salah.";
        } else if (e instanceof FirebaseAuthUserCollisionException) {
            return "Email sudah terdaftar.";
        } else if (e instanceof FirebaseAuthInvalidUserException) {
            return "Pengguna tidak ditemukan.";
        } else if (e != null && e.getMessage() != null && e.getMessage().contains("network")) {
            return "Tidak ada koneksi internet.";
        } else {
            String message = (e != null) ? e.getLocalizedMessage() : null;
            return (message != null) ? message : "Terjadi kesalahan pada Firebase.";
        }
    }

    public static class AuthRepoResult {
        public final boolean isSuccess;
        public final String uid;
        public final String errorMessage;

        private AuthRepoResult(boolean isSuccess, String uid, String errorMessage) {
            this.isSuccess = isSuccess;
            this.uid = uid;
            this.errorMessage = errorMessage;
        }

        public static AuthRepoResult success(String uid) {
            return new AuthRepoResult(true, uid, null);
        }

        public static AuthRepoResult error(String message) {
            return new AuthRepoResult(false, null, message);
        }
    }
}
