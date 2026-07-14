package com.example.java3.presentation.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.java3.R;
import com.example.java3.core.utils.SessionManager;
import com.example.java3.data.repository.AuthRepository;
import com.example.java3.databinding.FragmentProfileBinding;
import com.example.java3.databinding.ItemProfileMenuBinding;
import com.example.java3.presentation.activities.LoginActivity;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private SessionManager sessionManager;
    private AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        authRepository = new AuthRepository();
        
        setupMenuRows();
        setupListeners();
        displayUserData();
    }

    private void setupMenuRows() {
        bindMenu(binding.menuProfile, R.drawable.ic_person_rounded, getString(R.string.profile_me));
        bindMenu(binding.menuFavorite, R.drawable.ic_heart_outline, getString(R.string.favorite_spots));
        bindMenu(binding.menuHistory, R.drawable.ic_history_rounded, getString(R.string.activity_history));
        bindMenu(binding.menuSettings, R.drawable.ic_settings_rounded, getString(R.string.settings));
        bindMenu(binding.menuHelp, R.drawable.ic_help_rounded, getString(R.string.help_support));
        bindMenu(binding.menuAbout, R.drawable.ic_info_rounded, getString(R.string.about_app));
    }

    private void bindMenu(ItemProfileMenuBinding rowBinding, int iconResId, String title) {
        rowBinding.ivMenuIcon.setImageResource(iconResId);
        rowBinding.tvMenuTitle.setText(title);
    }

    private void setupListeners() {
        binding.btnProfileMore.setOnClickListener(v -> showDemoMessage());
        binding.menuProfile.getRoot().setOnClickListener(v -> showDemoMessage());
        binding.menuFavorite.getRoot().setOnClickListener(v -> showDemoMessage());
        binding.menuHistory.getRoot().setOnClickListener(v -> showDemoMessage());
        binding.menuSettings.getRoot().setOnClickListener(v -> showDemoMessage());
        binding.menuHelp.getRoot().setOnClickListener(v -> showDemoMessage());
        binding.menuAbout.getRoot().setOnClickListener(v -> showDemoMessage());
        binding.btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void displayUserData() {
        if (sessionManager.isLoggedIn()) {
            binding.tvProfileUsername.setText(sessionManager.getUserEmail());
        }
    }

    private void handleLogout() {
        authRepository.logout();
        sessionManager.clearSession();
        
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showDemoMessage() {
        Toast.makeText(requireContext(), "Fungsi ini akan segera tersedia.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
