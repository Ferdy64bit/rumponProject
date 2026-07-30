package com.example.java3.presentation.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.java3.R;
import com.example.java3.data.repository.CommunityRepository;
import com.example.java3.databinding.FragmentCommunityBinding;
import com.example.java3.domain.model.CommunityComment;
import com.example.java3.domain.model.Post;
import com.example.java3.presentation.adapters.PostAdapter;
import com.example.java3.presentation.model.CommunityPostUiModel;
import com.example.java3.presentation.viewmodels.CommunityViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CommunityFragment extends Fragment {
    private FragmentCommunityBinding binding;
    private CommunityViewModel viewModel;
    private PostAdapter adapter;
    private final List<Post> allPosts = new ArrayList<>();
    private String selectedFilter = "all";
    private String searchQuery = "";
    private Toast currentToast;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCommunityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CommunityViewModel.class);
        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        binding.rvCommunity.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PostAdapter(new ArrayList<>(), new PostAdapter.PostActionListener() {
            @Override
            public void onLikeClick(CommunityPostUiModel post) {
                viewModel.toggleLike(post.getId());
            }

            @Override
            public void onCommentClick(CommunityPostUiModel post) {
                showCommentsDialog(post);
            }

            @Override
            public void onShareClick(CommunityPostUiModel post) {
                sharePost(post);
            }

            @Override
            public void onBookmarkClick(CommunityPostUiModel post) {
                viewModel.toggleFavorite(post.getId());
            }

            @Override
            public void onDetailClick(CommunityPostUiModel post) {
                showPostDetail(post);
            }

            @Override
            public void onMoreClick(CommunityPostUiModel post, View anchor) {
                showPostMenu(post, anchor);
            }
        });
        binding.rvCommunity.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnCommunitySearch.setOnClickListener(v -> showSearchDialog());
        binding.fabAddPost.setOnClickListener(v -> openCreatePost());
        binding.chipAll.setOnClickListener(v -> {
            selectedFilter = "all";
            applyPosts();
        });
        binding.chipLatest.setOnClickListener(v -> {
            selectedFilter = "latest";
            applyPosts();
        });
        binding.chipPopular.setOnClickListener(v -> {
            selectedFilter = "popular";
            applyPosts();
        });
        binding.chipFollowing.setOnClickListener(v -> {
            selectedFilter = "following";
            applyPosts();
        });
    }

    private void openCreatePost() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new CreatePostFragment())
                .addToBackStack(null)
                .commit();
    }

    private void observeViewModel() {
        viewModel.getPostsLiveData().observe(getViewLifecycleOwner(), posts -> {
            allPosts.clear();
            if (posts != null) {
                allPosts.addAll(posts);
            }
            applyPosts();
        });
        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), this::showMessage);
    }

    private void applyPosts() {
        List<Post> visiblePosts = new ArrayList<>(allPosts);
        if (!searchQuery.trim().isEmpty()) {
            String query = searchQuery.trim().toLowerCase(Locale.getDefault());
            List<Post> searched = new ArrayList<>();
            for (Post post : visiblePosts) {
                if (matchesSearch(post, query)) {
                    searched.add(post);
                }
            }
            visiblePosts = searched;
        }

        if ("latest".equals(selectedFilter)) {
            Collections.sort(visiblePosts, (a, b) -> Long.compare(timeOf(b), timeOf(a)));
        } else if ("popular".equals(selectedFilter)) {
            Collections.sort(visiblePosts, Comparator.comparingInt(Post::getLikesCount).reversed());
        } else if ("following".equals(selectedFilter)) {
            String currentUserId = viewModel.getCurrentUserId();
            List<Post> ownPosts = new ArrayList<>();
            for (Post post : visiblePosts) {
                if (currentUserId != null && currentUserId.equals(post.getUserId())) {
                    ownPosts.add(post);
                }
            }
            visiblePosts = ownPosts;
        }

        adapter.submitList(mapPosts(visiblePosts));
    }

    private boolean matchesSearch(Post post, String query) {
        return contains(post.getUserName(), query)
                || contains(post.getCaption(), query)
                || contains(post.getFishType(), query)
                || contains(post.getLocationName(), query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.getDefault()).contains(query);
    }

    private long timeOf(Post post) {
        return post.getTimestamp() != null ? post.getTimestamp().getTime() : 0L;
    }

    private List<CommunityPostUiModel> mapPosts(List<Post> posts) {
        List<CommunityPostUiModel> uiModels = new ArrayList<>();
        if (posts == null) {
            return uiModels;
        }

        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            uiModels.add(new CommunityPostUiModel(
                    post.getId(),
                    nonBlank(post.getUserName(), "Pemancing"),
                    post.getTimestamp() != null ? formatTime(post.getTimestamp().getTime()) : "Baru saja",
                    nonBlank(post.getCaption(), ""),
                    post.getLikesCount(),
                    post.getCommentsCount(),
                    i % 2 == 0 ? R.drawable.img_avatar_angler : R.drawable.img_avatar_blue,
                    i % 2 == 0 ? R.drawable.img_post_fish : R.drawable.img_post_boat,
                    post.getUserProfilePic(),
                    post.getImageUrl(),
                    nonBlank(post.getFishType(), "-"),
                    nonBlank(post.getLocationName(), "Lokasi tidak diketahui"),
                    nonBlank(post.getWeatherCondition(), "-"),
                    nonBlank(post.getTideStatus(), "-"),
                    post.isLiked(),
                    post.isFavorite(),
                    post.getUserId()
            ));
        }
        return uiModels;
    }

    private String formatTime(long timestamp) {
        if (timestamp <= 0) {
            return "Baru saja";
        }
        CharSequence relativeTime = android.text.format.DateUtils.getRelativeTimeSpanString(
                timestamp,
                System.currentTimeMillis(),
                android.text.format.DateUtils.MINUTE_IN_MILLIS
        );
        return relativeTime.toString();
    }

    private String nonBlank(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }

    private void showSearchDialog() {
        EditText input = new EditText(requireContext());
        input.setSingleLine(true);
        input.setHint("Cari nama, caption, ikan, lokasi");
        input.setText(searchQuery);
        input.setSelectAllOnFocus(true);
        int padding = dp(20);
        input.setPadding(padding, dp(8), padding, dp(8));

        new AlertDialog.Builder(requireContext())
                .setTitle("Cari Postingan")
                .setView(input)
                .setPositiveButton("Cari", (dialog, which) -> {
                    searchQuery = input.getText() != null ? input.getText().toString() : "";
                    applyPosts();
                })
                .setNegativeButton("Reset", (dialog, which) -> {
                    searchQuery = "";
                    applyPosts();
                })
                .show();
    }

    private void showCommentsDialog(CommunityPostUiModel post) {
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(10), dp(20), 0);

        LinearLayout commentsContainer = new LinearLayout(requireContext());
        commentsContainer.setOrientation(LinearLayout.VERTICAL);
        TextView loading = createBodyText("Memuat komentar...");
        commentsContainer.addView(loading);

        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.addView(commentsContainer);
        content.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(220)
        ));

        EditText input = new EditText(requireContext());
        input.setHint("Tulis komentar...");
        input.setMinLines(2);
        input.setMaxLines(4);
        content.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Komentar")
                .setView(content)
                .setPositiveButton("Kirim", null)
                .setNegativeButton("Tutup", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String comment = input.getText() != null ? input.getText().toString().trim() : "";
                if (comment.isEmpty()) {
                    input.setError("Komentar tidak boleh kosong");
                    return;
                }
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                viewModel.addComment(post.getId(), comment, new CommunityRepository.RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        showMessage("Komentar terkirim");
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(String message) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        showMessage(message);
                    }
                });
            });
        });

        dialog.show();
        loadComments(post.getId(), commentsContainer);
    }

    private void loadComments(String postId, LinearLayout commentsContainer) {
        viewModel.getComments(postId, new CommunityRepository.RepositoryCallback<List<CommunityComment>>() {
            @Override
            public void onSuccess(List<CommunityComment> comments) {
                if (!isAdded()) {
                    return;
                }
                commentsContainer.removeAllViews();
                if (comments == null || comments.isEmpty()) {
                    commentsContainer.addView(createBodyText("Belum ada komentar."));
                    return;
                }
                for (CommunityComment comment : comments) {
                    TextView item = createBodyText(nonBlank(comment.getUserName(), "Pemancing") + ": " + nonBlank(comment.getText(), ""));
                    item.setPadding(0, dp(6), 0, dp(6));
                    commentsContainer.addView(item);
                }
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                commentsContainer.removeAllViews();
                commentsContainer.addView(createBodyText(message));
            }
        });
    }

    private void sharePost(CommunityPostUiModel post) {
        String shareText = "Fishing Point Tanjung Anom\n\n"
                + nonBlank(post.getUserName(), "Pemancing") + " membagikan tangkapan:\n"
                + nonBlank(post.getCaption(), "-") + "\n\n"
                + "Jenis ikan: " + nonBlank(post.getFishType(), "-") + "\n"
                + "Lokasi: " + nonBlank(post.getLocationName(), "-") + "\n"
                + "Cuaca: " + nonBlank(post.getWeather(), "-") + "\n"
                + "Perairan: " + nonBlank(post.getTide(), "-") + "\n"
                + (post.getPostImageUrl() != null && !post.getPostImageUrl().trim().isEmpty()
                ? "\nFoto: " + post.getPostImageUrl()
                : "");

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        try {
            startActivity(Intent.createChooser(intent, "Bagikan postingan"));
        } catch (Exception e) {
            showMessage("Tidak ada aplikasi untuk membagikan postingan.");
        }
    }

    private void showPostDetail(CommunityPostUiModel post) {
        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(12), dp(24), dp(8));
        scrollView.addView(content);

        addDetailRow(content, "Pemancing", post.getUserName());
        addDetailRow(content, "Waktu", post.getTime());
        addDetailRow(content, "Caption", post.getCaption());
        addDetailRow(content, "Jenis ikan", post.getFishType());
        addDetailRow(content, "Lokasi", post.getLocationName());
        addDetailRow(content, "Cuaca", post.getWeather());
        addDetailRow(content, "Kondisi perairan", post.getTide());
        addDetailRow(content, "Like", String.valueOf(post.getLikeCount()));
        addDetailRow(content, "Komentar", String.valueOf(post.getCommentCount()));

        new AlertDialog.Builder(requireContext())
                .setTitle("Detail Tangkapan")
                .setView(scrollView)
                .setPositiveButton("Tutup", null)
                .setNegativeButton("Bagikan", (dialog, which) -> sharePost(post))
                .show();
    }

    private void addDetailRow(LinearLayout content, String label, String value) {
        TextView title = createBodyText(label);
        title.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_secondary));
        title.setTextSize(12f);
        content.addView(title);

        TextView body = createBodyText(nonBlank(value, "-"));
        body.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_primary));
        body.setTextSize(15f);
        body.setPadding(0, 0, 0, dp(12));
        content.addView(body);
    }

    private void showPostMenu(CommunityPostUiModel post, View anchor) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchor);
        popupMenu.getMenu().add("Detail tangkapan");
        popupMenu.getMenu().add("Komentar");
        popupMenu.getMenu().add("Bagikan");
        String currentUserId = viewModel.getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(post.getUserId())) {
            popupMenu.getMenu().add("Hapus postingan");
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if ("Detail tangkapan".equals(title)) {
                showPostDetail(post);
            } else if ("Komentar".equals(title)) {
                showCommentsDialog(post);
            } else if ("Bagikan".equals(title)) {
                sharePost(post);
            } else if ("Hapus postingan".equals(title)) {
                confirmDelete(post);
            }
            return true;
        });
        popupMenu.show();
    }

    private void confirmDelete(CommunityPostUiModel post) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus Postingan")
                .setMessage("Postingan ini akan dihapus dari Firestore. Foto Cloudinary tetap aman dan dapat dibersihkan lewat backend nanti.")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    Post deleteTarget = new Post();
                    deleteTarget.setId(post.getId());
                    deleteTarget.setUserId(post.getUserId());
                    viewModel.deletePost(deleteTarget);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private TextView createBodyText(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextSize(14f);
        textView.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_primary));
        return textView;
    }

    private void showMessage(String message) {
        if (!isAdded() || message == null || message.trim().isEmpty()) {
            return;
        }
        if (currentToast != null) {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentToast != null) {
            currentToast.cancel();
            currentToast = null;
        }
        binding = null;
    }
}
