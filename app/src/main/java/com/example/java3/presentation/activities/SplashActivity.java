package com.example.java3.presentation.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.example.java3.R;
import com.example.java3.databinding.ActivitySplashBinding;
import com.example.java3.core.utils.SessionManager;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        setupSystemBars();
        playIntroAnimation();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (sessionManager.isLoggedIn()) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2400);
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );
    }

    private void playIntroAnimation() {
        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_fade_scale);
        Animation contentAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_slide_up);

        binding.ivLogo.startAnimation(logoAnimation);
        binding.tvBrandTitle.startAnimation(contentAnimation);
        binding.tvBrandSubtitle.startAnimation(contentAnimation);
        binding.tvTagline.startAnimation(contentAnimation);
        binding.progressBar.startAnimation(contentAnimation);
        binding.tvPoweredBy.startAnimation(contentAnimation);
    }
}
