package com.example.java3.presentation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.java3.R;
import com.example.java3.databinding.ItemFishingPointBinding;
import com.example.java3.domain.model.FishingPointWithRecommendation;

import java.util.List;
import java.util.Locale;

public class FishingPointAdapter extends RecyclerView.Adapter<FishingPointAdapter.ViewHolder> {
    private final List<FishingPointWithRecommendation> items;

    public FishingPointAdapter(List<FishingPointWithRecommendation> items) {
        this.items = items;
    }

    public void updateData(List<FishingPointWithRecommendation> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFishingPointBinding binding = ItemFishingPointBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFishingPointBinding binding;

        ViewHolder(ItemFishingPointBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(FishingPointWithRecommendation item, int position) {
            binding.tvSpotName.setText(item.getFishingPoint().getName());
            binding.tvRating.setText(String.format(Locale.getDefault(), "%.1f", item.getFishingPoint().getRating()));
            binding.tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", item.getDistance()));
            binding.tvRecommendationText.setText(createStars(item.getRecommendation().getStars()));

            String imageUrl = item.getFishingPoint().getImageUrl();
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                binding.ivSpotImage.setImageResource(getPlaceholder(position));
            } else {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(getPlaceholder(position))
                        .error(getPlaceholder(position))
                        .into(binding.ivSpotImage);
            }
        }

        private int getPlaceholder(int position) {
            int[] images = {
                    R.drawable.img_spot_dermaga,
                    R.drawable.img_spot_breakwater,
                    R.drawable.img_spot_muara
            };
            return images[position % images.length];
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
