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
import com.example.java3.databinding.ActivityLoginBinding;
import com.example.java3.presentation.viewmodels.LoginViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
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
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupSystemBars();
        playIntroAnimation();
        setupGoogleSignIn();
        setupListeners();
        observeViewModel();
        checkAutoLogin();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void checkAutoLogin() {
        if (viewModel.isLoggedIn()) {
            proceedToHome(viewModel.getUserUid(), viewModel.getUserEmail());
        }
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    }

    private void playIntroAnimation() {
        binding.llLogo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.splash_fade_scale));
        binding.loginPanel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.splash_slide_up));
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> handleLogin());
        binding.btnGoogle.setOnClickListener(v -> googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));
        binding.btnFacebook.setOnClickListener(v -> showDemoMessage());
        binding.tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void observeViewModel() {
        viewModel.getAuthResult().observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);
            binding.btnGoogle.setEnabled(true);

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

        viewModel.getResetPasswordResult().observe(this, message ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        );
    }

    private boolean isGoogleUser(FirebaseUser user) {
        for (UserInfo profile : user.getProviderData()) {
            if (profile.getProviderId().equals(GoogleAuthProvider.PROVIDER_ID)) {
                return true;
            }
        }
        return false;
    }

    private void showVerificationDialog(FirebaseUser user) {
        if (user == null || isFinishing()) return;

        // Using standard AlertDialog to prevent Resources$NotFoundException: Resource ID #0x0
        // which can happen if the Material3 theme overlay is broken or missing attributes.
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Email Belum Terverifikasi")
                .setMessage("Silakan verifikasi email Anda sebelum masuk.")
                .setPositiveButton("Kirim Ulang", (dialog, which) -> {
                    user.sendEmailVerification()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Email verifikasi telah dikirim ulang.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Gagal mengirim email verifikasi.", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Tutup", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    if (googleSignInClient != null) {
                        googleSignInClient.signOut();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void proceedToHome(String uid, String email) {
        viewModel.saveSession(uid, email);
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
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

    private void handleLogin() {
        String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.etPassword.getText()).toString().trim();

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);
        viewModel.login(email, password);
    }

    private void handleForgotPassword() {
        String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();
        viewModel.forgotPassword(email);
    }

    private void showDemoMessage() {
        Toast.makeText(this, "Login sosial akan diaktifkan setelah konfirmasi.", Toast.LENGTH_SHORT).show();
    }
}
