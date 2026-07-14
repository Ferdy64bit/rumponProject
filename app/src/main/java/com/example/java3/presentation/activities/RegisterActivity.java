package com.example.java3.presentation.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.java3.R;
import com.example.java3.databinding.ActivityRegisterBinding;
import com.example.java3.presentation.viewmodels.RegisterViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignInResult(task);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        setupSystemBars();
        playIntroAnimation();
        setupGoogleSignIn();
        setupListeners();
        observeViewModel();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    }

    private void playIntroAnimation() {
        binding.llLogo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.splash_fade_scale));
        binding.registerPanel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.splash_slide_up));
    }

    private void setupListeners() {
        binding.btnRegister.setOnClickListener(v -> handleRegister());
        binding.btnGoogle.setOnClickListener(v -> googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));
        binding.tvLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void observeViewModel() {
        viewModel.getAuthResult().observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnRegister.setEnabled(true);

            if (result.isSuccess) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // Email verification check disabled temporarily as requested
                    proceedToHome(result.uid, user.getEmail());
                }
            } else {
                Toast.makeText(this, result.errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isGoogleUser(FirebaseUser user) {
        for (UserInfo profile : user.getProviderData()) {
            if (profile.getProviderId().equals(GoogleAuthProvider.PROVIDER_ID)) {
                return true;
            }
        }
        return false;
    }

    private void proceedToHome(String uid, String email) {
        viewModel.saveSession(uid, email);
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finishAffinity();
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnGoogle.setEnabled(false);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                viewModel.signInWithGoogle(credential);
            }
        } catch (ApiException e) {
            Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Registrasi Berhasil")
                .setMessage("Akun Anda telah berhasil dibuat. Silakan cek email Anda untuk melakukan verifikasi sebelum masuk.")
                .setPositiveButton("Ke Halaman Login", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                })
                .setCancelable(false)
                .show();
    }

    private void handleRegister() {
        String fullName = Objects.requireNonNull(binding.etFullName.getText()).toString().trim();
        String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.etPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(binding.etConfirmPassword.getText()).toString().trim();

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRegister.setEnabled(false);
        viewModel.register(fullName, email, password, confirmPassword);
    }
}
