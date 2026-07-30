package com.example.java3.presentation.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
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
        List<SpotUiModel> nextItems = newItems != null ? new ArrayList<>(newItems) : new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SpotDiffCallback(items, nextItems));
        items.clear();
        items.addAll(nextItems);
        diffResult.dispatchUpdatesTo(this);
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
                        .centerCrop()
                        .thumbnail(0.25f)
                        .into(binding.ivSpotImage);
            }
            binding.tvSpotName.setText(item.getName());
            binding.tvDistance.setText(String.format(Locale.getDefault(), "%.1f km dari lokasi Anda", item.getDistance()));
            binding.tvSpotMeta.setText(item.getType());
            binding.getRoot().setContentDescription(String.format(
                    Locale.getDefault(),
                    "%s, %.1f kilometer dari lokasi Anda. Ketuk untuk melihat rekomendasi live.",
                    item.getName(),
                    item.getDistance()
            ));
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSpotClick(item);
                }
            });
        }
    }

    private static class SpotDiffCallback extends DiffUtil.Callback {
        private final List<SpotUiModel> oldItems;
        private final List<SpotUiModel> newItems;

        SpotDiffCallback(List<SpotUiModel> oldItems, List<SpotUiModel> newItems) {
            this.oldItems = oldItems;
            this.newItems = newItems;
        }

        @Override
        public int getOldListSize() {
            return oldItems.size();
        }

        @Override
        public int getNewListSize() {
            return newItems.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            String oldId = oldItems.get(oldItemPosition).getId();
            String newId = newItems.get(newItemPosition).getId();
            if (oldId != null && newId != null && !oldId.isEmpty() && !newId.isEmpty()) {
                return oldId.equals(newId);
            }
            return oldItems.get(oldItemPosition).getName().equals(newItems.get(newItemPosition).getName());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            SpotUiModel oldItem = oldItems.get(oldItemPosition);
            SpotUiModel newItem = newItems.get(newItemPosition);
            return oldItem.getName().equals(newItem.getName())
                    && oldItem.getImageResId() == newItem.getImageResId()
                    && safeEquals(oldItem.getImageUrl(), newItem.getImageUrl())
                    && safeEquals(oldItem.getType(), newItem.getType())
                    && Math.abs(oldItem.getDistance() - newItem.getDistance()) < 0.05;
        }

        private boolean safeEquals(String first, String second) {
            if (first == null) return second == null;
            return first.equals(second);
        }
    }
}