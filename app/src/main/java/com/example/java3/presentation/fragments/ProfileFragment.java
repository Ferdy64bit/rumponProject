package com.example.java3.presentation.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.java3.R;
import com.example.java3.core.utils.SessionManager;
import com.example.java3.data.repository.AuthRepository;
import com.example.java3.databinding.FragmentProfileBinding;
import com.example.java3.databinding.ItemProfileMenuBinding;
import com.example.java3.domain.model.FishingPoint;
import com.example.java3.domain.model.Post;
import com.example.java3.presentation.activities.LoginActivity;
import com.example.java3.presentation.model.ProfileStatsUiModel;
import com.example.java3.presentation.model.ProfileUiModel;
import com.example.java3.presentation.viewmodels.ProfileViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    private static final long MAX_PROFILE_PHOTO_BYTES = 5L * 1024L * 1024L;

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;
    private AuthRepository authRepository;
    private ProfileViewModel viewModel;
    private ProfileUiModel currentProfile;
    private ProfileStatsUiModel currentStats;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handleSelectedPhoto(uri);
                    }
                }
            }
    );

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
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupMenuRows();
        setupListeners();
        observeViewModel();
    }

    private void setupMenuRows() {
        bindMenu(binding.menuProfile, R.drawable.ic_person_rounded, "Edit Profile");
        bindMenu(binding.menuStats, R.drawable.ic_fish_symbol, "Statistik Memancing");
        bindMenu(binding.menuMySpots, R.drawable.ic_location_pin, "Spot Saya");
        bindMenu(binding.menuFavorite, R.drawable.ic_heart_outline, "Spot Favorit");
        bindMenu(binding.menuMyPosts, R.drawable.ic_bookmark_outline, "Postingan Saya");
        bindMenu(binding.menuSettings, R.drawable.ic_settings_rounded, getString(R.string.settings));
        bindMenu(binding.menuSecurity, R.drawable.ic_lock_rounded, "Keamanan");
        bindMenu(binding.menuAbout, R.drawable.ic_info_rounded, getString(R.string.about_app));
    }

    private void bindMenu(ItemProfileMenuBinding rowBinding, int iconResId, String title) {
        rowBinding.ivMenuIcon.setImageResource(iconResId);
        rowBinding.tvMenuTitle.setText(title);
    }

    private void setupListeners() {
        binding.btnProfileMore.setOnClickListener(v -> showPhotoBottomSheet());
        binding.ivProfileLarge.setOnClickListener(v -> showPhotoBottomSheet());
        binding.menuProfile.getRoot().setOnClickListener(v -> showEditProfileDialog());
        binding.menuStats.getRoot().setOnClickListener(v -> showStatsDialog());
        binding.menuMySpots.getRoot().setOnClickListener(v -> viewModel.loadMySpots());
        binding.menuFavorite.getRoot().setOnClickListener(v -> viewModel.loadFavoriteSpots());
        binding.menuMyPosts.getRoot().setOnClickListener(v -> viewModel.loadMyPosts());
        binding.menuSettings.getRoot().setOnClickListener(v -> showSettingsDialog());
        binding.menuSecurity.getRoot().setOnClickListener(v -> showSecurityDialog());
        binding.menuAbout.getRoot().setOnClickListener(v -> showAboutDialog());
        binding.btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void observeViewModel() {
        viewModel.getProfileLiveData().observe(getViewLifecycleOwner(), profile -> {
            currentProfile = profile;
            renderProfile(profile);
        });
        viewModel.getStatsLiveData().observe(getViewLifecycleOwner(), stats -> {
            currentStats = stats;
            renderStats(stats);
        });
        viewModel.getUploadingLiveData().observe(getViewLifecycleOwner(), uploading -> {
            if (binding != null) {
                binding.progressPhotoUpload.setVisibility(Boolean.TRUE.equals(uploading) ? View.VISIBLE : View.GONE);
                binding.btnProfileMore.setEnabled(!Boolean.TRUE.equals(uploading));
            }
        });
        viewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), loading -> {
            binding.menuProfile.getRoot().setEnabled(!Boolean.TRUE.equals(loading));
        });
        viewModel.getMessageLiveData().observe(getViewLifecycleOwner(), this::showMessage);
        viewModel.getMyPostsLiveData().observe(getViewLifecycleOwner(), this::showMyPostsDialog);
        viewModel.getMySpotsLiveData().observe(getViewLifecycleOwner(), this::showMySpotsDialog);
        viewModel.getFavoriteSpotsLiveData().observe(getViewLifecycleOwner(), this::showFavoriteSpotsDialog);
    }

    private void renderProfile(ProfileUiModel profile) {
        if (profile == null || binding == null) {
            return;
        }
        binding.tvProfileName.setText(nonBlank(profile.getName(), "Pemancing"));
        binding.tvProfileUsername.setText(nonBlank(profile.getEmail(), sessionManager.getUserEmail()));
        binding.tvProfileBio.setText(nonBlank(profile.getBio(), "Fishing Point Member"));
        Glide.with(this)
                .load(profile.getPhotoUrl())
                .placeholder(R.drawable.img_avatar_angler)
                .error(R.drawable.img_avatar_angler)
                .into(binding.ivProfileLarge);
    }

    private void renderStats(ProfileStatsUiModel stats) {
        if (stats == null || binding == null) {
            return;
        }
        binding.tvStatPosts.setText(String.valueOf(stats.getPostCount()));
        binding.tvStatSpots.setText(String.valueOf(stats.getSpotCount()));
        binding.tvStatFavorites.setText(String.valueOf(stats.getFavoriteCount()));
    }

    private void showPhotoBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(16), dp(20), dp(20));

        TextView title = createText("Foto Profil", 18, true);
        content.addView(title);
        content.addView(createActionText("Pilih dari Gallery", v -> {
            dialog.dismiss();
            openImagePicker();
        }));
        content.addView(createActionText("Edit Data Profile", v -> {
            dialog.dismiss();
            showEditProfileDialog();
        }));
        dialog.setContentView(content);
        dialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void handleSelectedPhoto(Uri uri) {
        long size = getUriSize(uri);
        if (size > MAX_PROFILE_PHOTO_BYTES) {
            showMessage("Ukuran foto maksimal 5MB");
            return;
        }
        try {
            viewModel.uploadProfilePhoto(readUriBytes(uri));
        } catch (IOException e) {
            showMessage("Foto tidak dapat dibaca.");
        }
    }

    private byte[] readUriBytes(Uri uri) throws IOException {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("Foto tidak ditemukan");
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            return outputStream.toByteArray();
        }
    }

    private long getUriSize(Uri uri) {
        try {
            android.content.res.AssetFileDescriptor descriptor = requireContext().getContentResolver().openAssetFileDescriptor(uri, "r");
            if (descriptor == null) {
                return 0L;
            }
            long length = descriptor.getLength();
            descriptor.close();
            return length;
        } catch (Exception e) {
            return 0L;
        }
    }

    private void showEditProfileDialog() {
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(8), dp(20), 0);

        EditText name = input("Nama", currentProfile != null ? currentProfile.getName() : "", InputType.TYPE_CLASS_TEXT);
        EditText phone = input("Nomor HP", currentProfile != null ? currentProfile.getPhone() : "", InputType.TYPE_CLASS_PHONE);
        EditText address = input("Alamat", currentProfile != null ? currentProfile.getAddress() : "", InputType.TYPE_CLASS_TEXT);
        EditText bio = input("Bio singkat", currentProfile != null ? currentProfile.getBio() : "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        bio.setMinLines(2);
        bio.setMaxLines(4);

        content.addView(name);
        content.addView(phone);
        content.addView(address);
        content.addView(bio);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Edit Profile")
                .setView(content)
                .setPositiveButton("Simpan", null)
                .setNegativeButton("Batal", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nameValue = read(name);
            String phoneValue = read(phone);
            String bioValue = read(bio);
            if (nameValue.isEmpty()) {
                name.setError("Nama tidak boleh kosong");
                return;
            }
            if (!phoneValue.isEmpty() && !phoneValue.matches("[0-9]+")) {
                phone.setError("Nomor HP hanya angka");
                return;
            }
            if (bioValue.length() > 150) {
                bio.setError("Bio maksimal 150 karakter");
                return;
            }
            viewModel.updateProfile(nameValue, phoneValue, read(address), bioValue);
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void showStatsDialog() {
        ProfileStatsUiModel stats = currentStats != null ? currentStats : new ProfileStatsUiModel(0, 0, 0, 0, 0, 0);
        String body = "Jumlah Spot Dibuat: " + stats.getSpotCount()
                + "\nJumlah Posting: " + stats.getPostCount()
                + "\nJumlah Like: " + stats.getLikeCount()
                + "\nJumlah Komentar: " + stats.getCommentCount()
                + "\nJumlah Favorite Spot: " + stats.getFavoriteCount()
                + "\nTanggal Bergabung: " + formatDate(stats.getJoinDate());
        showTextDialog("Statistik Memancing", body);
    }

    private void showMySpotsDialog(List<FishingPoint> points) {
        StringBuilder builder = new StringBuilder();
        if (points == null || points.isEmpty()) {
            builder.append("Belum ada spot yang kamu buat.");
        } else {
            for (FishingPoint point : points) {
                builder.append("- ").append(nonBlank(point.getName(), "Spot memancing"))
                        .append("\n  Jenis: ").append(nonBlank(point.getType(), "Belum diisi"))
                        .append(" | Akses: ").append(formatVisibility(point.getVisibility()))
                        .append("\n\n");
            }
        }
        showTextDialog("Spot Saya", builder.toString().trim());
    }

    private void showFavoriteSpotsDialog(List<FishingPoint> points) {
        StringBuilder builder = new StringBuilder();
        if (points == null || points.isEmpty()) {
            builder.append("Belum ada spot favorit.");
        } else {
            for (FishingPoint point : points) {
                builder.append("- ").append(nonBlank(point.getName(), "Spot memancing"))
                        .append("\n  Lokasi: ").append(nonBlank(point.getArea(), nonBlank(point.getLocationName(), point.getType())))
                        .append("\n\n");
            }
        }
        showTextDialog("Spot Favorit", builder.toString().trim());
    }

    private void showMyPostsDialog(List<Post> posts) {
        StringBuilder builder = new StringBuilder();
        if (posts == null || posts.isEmpty()) {
            builder.append("Belum ada postingan komunitas.");
        } else {
            for (Post post : posts) {
                builder.append("- ").append(nonBlank(post.getCaption(), "Postingan tangkapan"))
                        .append("\n  Ikan: ").append(nonBlank(post.getFishType(), "Belum diisi"))
                        .append(" | ").append(post.getLikesCount()).append(" like")
                        .append(" | ").append(post.getCommentsCount()).append(" komentar")
                        .append("\n\n");
            }
        }
        showTextDialog("Postingan Saya", builder.toString().trim());
    }
    private void showSettingsDialog() {
        String[] options = {"Tema mengikuti sistem", "Notifikasi aktif", "Bahasa Indonesia"};
        boolean[] checked = {true, true, true};
        new AlertDialog.Builder(requireContext())
                .setTitle("Pengaturan")
                .setMultiChoiceItems(options, checked, null)
                .setPositiveButton("Simpan", (dialog, which) -> showMessage("Pengaturan tersimpan di perangkat ini."))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showSecurityDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Keamanan")
                .setItems(new String[]{"Reset Password via Email"}, (dialog, which) -> viewModel.sendPasswordReset())
                .show();
    }

    private void showAboutDialog() {
        showTextDialog("Tentang Aplikasi", "Fishing Point Tanjung Anom\nVersi 1.0\nDeveloper: Ferdy Ahmad Winata\nUniversitas: UNIS Tangerang");
    }

    private void showTextDialog(String title, String body) {
        body = sanitizeDialogText(body);
        ScrollView scrollView = new ScrollView(requireContext());
        TextView textView = createText(body, 14, false);
        textView.setPadding(dp(22), dp(10), dp(22), dp(8));
        scrollView.addView(textView);
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(scrollView)
                .setPositiveButton("Tutup", null)
                .show();
    }

    private void handleLogout() {
        authRepository.logout();
        sessionManager.clearSession();

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private EditText input(String hint, String value, int inputType) {
        EditText editText = new EditText(requireContext());
        editText.setHint(hint);
        editText.setText(value);
        editText.setInputType(inputType);
        editText.setSingleLine((inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) == 0);
        editText.setPadding(0, dp(8), 0, dp(8));
        return editText;
    }

    private TextView createText(String text, int sp, boolean bold) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextSize(sp);
        textView.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_primary));
        if (bold) {
            textView.setTypeface(textView.getTypeface(), android.graphics.Typeface.BOLD);
        }
        return textView;
    }

    private TextView createActionText(String text, View.OnClickListener listener) {
        TextView textView = createText(text, 16, false);
        textView.setMinHeight(dp(52));
        textView.setGravity(android.view.Gravity.CENTER_VERTICAL);
        textView.setOnClickListener(listener);
        return textView;
    }

    private String read(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String nonBlank(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }

    private String formatVisibility(String visibility) {
        String normalized = visibility != null ? visibility.trim().toUpperCase(Locale.US) : "";
        if ("PRIVATE".equals(normalized) || "PRIVAT".equals(normalized)) {
            return "Pribadi";
        }
        if ("PUBLIC".equals(normalized) || "PUBLIK".equals(normalized) || "UMUM".equals(normalized)) {
            return "Publik";
        }
        return nonBlank(visibility, "Publik");
    }

    private String sanitizeDialogText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }
        return value
                .replace("ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¢", "-")
                .replace("ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢", "-")
                .replace("â€¢", "-")
                .replace(" ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ ", " | ")
                .replace(" ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¢ ", " | ")
                .replace("  ", " ");
    }

    private String formatDate(long millis) {
        if (millis <= 0) {
            return "-";
        }
        return DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale("id", "ID")).format(new Date(millis));
    }

    private void showMessage(String message) {
        if (binding == null || message == null || message.trim().isEmpty()) {
            return;
        }
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


