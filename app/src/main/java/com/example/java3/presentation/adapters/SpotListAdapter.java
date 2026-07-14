package com.example.java3.presentation.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.java3.databinding.ItemSpotListBinding;
import com.example.java3.presentation.model.SpotUiModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpotListAdapter extends RecyclerView.Adapter<SpotListAdapter.ViewHolder> {
    private final List<SpotUiModel> items = new ArrayList<>();
    private OnSpotClickListener onSpotClickListener;

    public interface OnSpotClickListener {
        void onSpotClick(SpotUiModel spot);
    }

    public void setOnSpotClickListener(OnSpotClickListener listener) {
        this.onSpotClickListener = listener;
    }

    public void submitList(List<SpotUiModel> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSpotListBinding binding = ItemSpotListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), onSpotClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSpotListBinding binding;

        ViewHolder(ItemSpotListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SpotUiModel item, OnSpotClickListener listener) {
            if (item.getImageUrl() == null || item.getImageUrl().trim().isEmpty()) {
                binding.ivSpotImage.setImageResource(item.getImageResId());
            } else {
                Glide.with(itemView.getContext())
                        .load(item.getImageUrl())
                        .placeholder(item.getImageResId())
                        .error(item.getImageResId())
                        .into(binding.ivSpotImage);
            }
            binding.tvSpotName.setText(item.getName());
            binding.tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", item.getDistance()));
            binding.tvRating.setText(String.format(Locale.getDefault(), "%.1f (%d)", item.getRating(), item.getReviewCount()));
            binding.tvBadge.setText(createStars(Math.round(item.getRating())));
            binding.tvBadge.setBackground(createBadgeBackground(item.getBadgeColor()));
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSpotClick(item);
                }
            });
        }

        private GradientDrawable createBadgeBackground(int color) {
            float radius = itemView.getResources().getDisplayMetrics().density * 999;
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(color);
            drawable.setCornerRadius(radius);
            return drawable;
        }

        private String createStars(int count) {
            StringBuilder stars = new StringBuilder();
            int safeCount = Math.max(1, Math.min(count, 5));
            for (int i = 0; i < safeCount; i++) {
                stars.append('\u2605');
            }
            return stars.toString();
        }
    }
}
