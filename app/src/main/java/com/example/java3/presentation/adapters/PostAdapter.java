package com.example.java3.presentation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.java3.databinding.ItemPostBinding;
import com.example.java3.presentation.model.CommunityPostUiModel;

import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private final List<CommunityPostUiModel> posts;

    public PostAdapter(List<CommunityPostUiModel> posts) {
        this.posts = posts;
    }

    public void submitList(List<CommunityPostUiModel> newPosts) {
        posts.clear();
        posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostBinding binding = ItemPostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.bind(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final ItemPostBinding binding;

        PostViewHolder(ItemPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CommunityPostUiModel post) {
            binding.tvUserName.setText(post.getUserName());
            binding.tvPostTime.setText(post.getTime());
            binding.tvCaption.setText(post.getCaption());
            binding.tvLikeCount.setText(String.format(Locale.getDefault(), "%d", post.getLikeCount()));
            binding.tvCommentCount.setText(String.format(Locale.getDefault(), "%d", post.getCommentCount()));
            loadImage(post.getAvatarUrl(), post.getAvatarResId(), true);
            loadImage(post.getPostImageUrl(), post.getPostImageResId(), false);
        }

        private void loadImage(String url, int placeholder, boolean avatar) {
            if (url == null || url.trim().isEmpty()) {
                if (avatar) {
                    binding.ivUserProfile.setImageResource(placeholder);
                } else {
                    binding.ivPostImage.setImageResource(placeholder);
                }
                return;
            }

            if (avatar) {
                Glide.with(itemView.getContext())
                        .load(url)
                        .placeholder(placeholder)
                        .error(placeholder)
                        .into(binding.ivUserProfile);
            } else {
                Glide.with(itemView.getContext())
                        .load(url)
                        .placeholder(placeholder)
                        .error(placeholder)
                        .into(binding.ivPostImage);
            }
        }
    }
}
