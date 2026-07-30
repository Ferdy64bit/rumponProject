package com.example.java3.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.java3.R;
import com.example.java3.databinding.ItemPostBinding;
import com.example.java3.presentation.model.CommunityPostUiModel;

import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private final List<CommunityPostUiModel> posts;
    private final PostActionListener listener;

    public PostAdapter(List<CommunityPostUiModel> posts, PostActionListener listener) {
        this.posts = posts;
        this.listener = listener;
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
        holder.bind(posts.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public interface PostActionListener {
        void onLikeClick(CommunityPostUiModel post);
        void onCommentClick(CommunityPostUiModel post);
        void onShareClick(CommunityPostUiModel post);
        void onBookmarkClick(CommunityPostUiModel post);
        void onDetailClick(CommunityPostUiModel post);
        void onMoreClick(CommunityPostUiModel post, View anchor);
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final ItemPostBinding binding;

        PostViewHolder(ItemPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CommunityPostUiModel post, PostActionListener listener) {
            binding.tvUserName.setText(post.getUserName());
            binding.tvPostTime.setText(post.getTime());
            binding.tvCaption.setText(post.getCaption());
            binding.tvLikeCount.setText(String.format(Locale.getDefault(), "%d", post.getLikeCount()));
            binding.tvCommentCount.setText(String.format(Locale.getDefault(), "%d", post.getCommentCount()));
            bindOptionalInfo(post);
            bindActionState(post);
            loadImage(post.getAvatarUrl(), post.getAvatarResId(), true);
            loadImage(post.getPostImageUrl(), post.getPostImageResId(), false);

            binding.btnLike.setOnClickListener(v -> listener.onLikeClick(post));
            binding.btnComment.setOnClickListener(v -> listener.onCommentClick(post));
            binding.btnShare.setOnClickListener(v -> listener.onShareClick(post));
            binding.btnBookmark.setOnClickListener(v -> listener.onBookmarkClick(post));
            binding.btnMore.setOnClickListener(v -> listener.onMoreClick(post, v));
            binding.ivPostImage.setOnClickListener(v -> listener.onDetailClick(post));
            binding.getRoot().setOnClickListener(v -> listener.onDetailClick(post));
        }

        private void bindOptionalInfo(CommunityPostUiModel post) {
            String fishType = trim(post.getFishType());
            String location = trim(post.getLocationName());

            binding.tvFishType.setVisibility(fishType.isEmpty() ? View.GONE : View.VISIBLE);
            binding.tvFishType.setText(fishType.isEmpty() ? "" : "Jenis ikan: " + fishType);

            binding.tvCatchMeta.setVisibility(location.isEmpty() ? View.GONE : View.VISIBLE);
            binding.tvCatchMeta.setText(location);

            binding.layoutCatchInfo.setVisibility((fishType.isEmpty() && location.isEmpty()) ? View.GONE : View.VISIBLE);
        }

        private void bindActionState(CommunityPostUiModel post) {
            int primary = ContextCompat.getColor(itemView.getContext(), R.color.primary);
            int textPrimary = ContextCompat.getColor(itemView.getContext(), R.color.text_primary);

            binding.btnLike.setImageResource(post.isLiked() ? R.drawable.ic_heart_rounded : R.drawable.ic_heart_outline);
            binding.btnLike.setColorFilter(post.isLiked() ? primary : textPrimary);
            binding.tvLikeCount.setTextColor(post.isLiked() ? primary : textPrimary);

            binding.btnBookmark.setImageResource(post.isFavorite() ? R.drawable.ic_bookmark_rounded : R.drawable.ic_bookmark_outline);
            binding.btnBookmark.setColorFilter(post.isFavorite() ? primary : textPrimary);
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
                        .centerCrop()
                        .into(binding.ivUserProfile);
            } else {
                Glide.with(itemView.getContext())
                        .load(url)
                        .placeholder(placeholder)
                        .error(placeholder)
                        .fitCenter()
                        .into(binding.ivPostImage);
            }
        }

        private String trim(String value) {
            return value != null ? value.trim() : "";
        }
    }
}
