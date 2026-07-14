package com.example.java3.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.java3.R;
import com.example.java3.databinding.FragmentCommunityBinding;
import com.example.java3.domain.model.Post;
import com.example.java3.presentation.adapters.PostAdapter;
import com.example.java3.presentation.model.CommunityPostUiModel;
import com.example.java3.presentation.viewmodels.CommunityViewModel;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {
    private FragmentCommunityBinding binding;
    private CommunityViewModel viewModel;
    private PostAdapter adapter;

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
        adapter = new PostAdapter(new ArrayList<>());
        binding.rvCommunity.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnCommunitySearch.setOnClickListener(v -> showUnavailableMessage());
        binding.fabAddPost.setOnClickListener(v -> showUnavailableMessage());
        binding.chipAll.setOnClickListener(v -> showUnavailableMessage());
        binding.chipLatest.setOnClickListener(v -> showUnavailableMessage());
        binding.chipPopular.setOnClickListener(v -> showUnavailableMessage());
        binding.chipFollowing.setOnClickListener(v -> showUnavailableMessage());
    }

    private void observeViewModel() {
        viewModel.getPostsLiveData().observe(getViewLifecycleOwner(), posts -> adapter.submitList(mapPosts(posts)));
    }

    private void showUnavailableMessage() {
        Toast.makeText(requireContext(), "Fitur komunitas ini belum tersedia.", Toast.LENGTH_SHORT).show();
    }

    private List<CommunityPostUiModel> mapPosts(List<Post> posts) {
        List<CommunityPostUiModel> uiModels = new ArrayList<>();
        if (posts == null) {
            return uiModels;
        }

        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            uiModels.add(new CommunityPostUiModel(
                    nonBlank(post.getUserName(), "Pemancing"),
                    formatTime(post.getTimestamp()),
                    nonBlank(post.getCaption(), ""),
                    post.getLikesCount(),
                    post.getCommentsCount(),
                    i % 2 == 0 ? R.drawable.img_avatar_angler : R.drawable.img_avatar_blue,
                    i % 2 == 0 ? R.drawable.img_post_fish : R.drawable.img_post_boat,
                    post.getUserProfilePic(),
                    post.getImageUrl()
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
